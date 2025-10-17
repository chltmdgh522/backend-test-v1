package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.partner.FeePolicy
import im.bigs.pg.domain.partner.Partner
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class 결제서비스Test {
    private val partnerRepo = mockk<PartnerOutPort>()
    private val feeRepo = mockk<FeePolicyOutPort>()
    private val paymentRepo = mockk<PaymentOutPort>()
    private val pgClient = object : PgClientOutPort {
        override fun supports(partnerId: Long) = true
        override fun approve(request: PgApproveRequest) =
            PgApproveResult("APPROVAL-123", LocalDateTime.of(2024,1,1,0,0), PaymentStatus.APPROVED)
    }

    @Test
    @DisplayName("결제 시 수수료 정책을 적용하고 저장해야 한다")
    fun `결제 시 수수료 정책을 적용하고 저장해야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)
        every { feeRepo.findEffectivePolicy(1L, any()) } returns FeePolicy(
            id = 10L, partnerId = 1L, effectiveFrom = LocalDateTime.ofInstant(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC),
            percentage = BigDecimal("0.0300"), fixedFee = BigDecimal("100")
        )
        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 99L) }

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "4242")
        val res = service.pay(cmd)

        assertEquals(99L, res.id)
        assertEquals(BigDecimal("400"), res.feeAmount)
        assertEquals(BigDecimal("9600"), res.netAmount)
        assertEquals(PaymentStatus.APPROVED, res.status)
    }

    @Test
    @DisplayName("제휴사별 수수료 정책을 적용해 계산해야 한다")
    fun `제휴사별 수수료 정책을 적용해 계산해야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))

        // ===== [Case 1] 파트너 1번: 2.35%, 고정 수수료 없음 =====
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)

        // 2.35%의 수수료율, 고정 수수료 없음
        every { feeRepo.findEffectivePolicy(1L, any()) } returns FeePolicy(
                id = 1L, partnerId = 1L, effectiveFrom = LocalDateTime.ofInstant(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC),
                percentage = BigDecimal("0.0235"), fixedFee = null // 0.03
        )

        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 99L) }

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "4242")
        val res = service.pay(cmd)

        assertEquals(99L, res.id)
        assertEquals(BigDecimal("0.0235"), res.appliedFeeRate)
        assertEquals(BigDecimal("235"), res.feeAmount) // 10000 * 0.0235 = 235
        assertEquals(BigDecimal("9765"), res.netAmount) // 10000 - 235 = 9765
        assertEquals(PaymentStatus.APPROVED, res.status)

        // ===== [Case 2] 파트너 2번: 3% + 고정 수수료 100원 =====
        every { partnerRepo.findById(2L) } returns Partner(2L, "ALPHA", "Alpha Partner", true)
        every { feeRepo.findEffectivePolicy(2L, any()) } returns FeePolicy(
                id = 2L,
                partnerId = 2L,
                effectiveFrom = LocalDateTime.ofInstant(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC),
                percentage = BigDecimal("0.03"),
                fixedFee = BigDecimal("100")
        )

        val savedSlot2 = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot2)) } answers { savedSlot2.captured.copy(id = 202L) }

        val cmd2 = PaymentCommand(partnerId = 2L, amount = BigDecimal("10000"), cardLast4 = "1111")
        val res2 = service.pay(cmd2)

        // 계산: (10000 * 0.03) + 100 = 300 + 100 = 400원 수수료
        assertEquals(202L, res2.id)
        assertEquals(BigDecimal("0.03"), res2.appliedFeeRate)
        assertEquals(BigDecimal("400"), res2.feeAmount)
        assertEquals(BigDecimal("9600"), res2.netAmount) // 10000 - 400 = 9600
        assertEquals(PaymentStatus.APPROVED, res2.status)
    }

    @Test
    @DisplayName("존재하지 않는 제휴사에 대해 예외를 던져야 한다")
    fun `존재하지 않는 제휴사에 대해 예외를 던져야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))
        every { partnerRepo.findById(999L) } returns null

        val cmd = PaymentCommand(partnerId = 999L, amount = BigDecimal("10000"))

        assertThrows<IllegalArgumentException> { service.pay(cmd) }
    }

    @Test
    @DisplayName("비활성 제휴사에 대해 예외를 던져야 한다")
    fun `비활성 제휴사에 대해 예외를 던져야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))
        every { partnerRepo.findById(2L) } returns Partner(2L, "INACTIVE", "Inactive Partner", false)

        val cmd = PaymentCommand(partnerId = 2L, amount = BigDecimal("10000"))

        assertThrows<IllegalArgumentException> { service.pay(cmd) }
    }

    @Test
    @DisplayName("수수료 정책이 없는 제휴사에 대해 예외를 던져야 한다")
    fun `수수료 정책이 없는 제휴사에 대해 예외를 던져야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))

        val partnerId = 1L

        every { partnerRepo.findById(partnerId) } returns Partner(partnerId, "NO_POLICY", "No Policy Partner", true)
        every { feeRepo.findEffectivePolicy(partnerId, any()) } returns null

        val cmd = PaymentCommand(partnerId = partnerId, amount = BigDecimal("10000"))

        assertThrows<IllegalStateException> { service.pay(cmd) }
    }
}

