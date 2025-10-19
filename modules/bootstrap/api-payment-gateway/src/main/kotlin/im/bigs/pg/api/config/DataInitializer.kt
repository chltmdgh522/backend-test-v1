package im.bigs.pg.api.config

import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.infra.persistence.partner.entity.FeePolicyEntity
import im.bigs.pg.infra.persistence.partner.entity.PartnerEntity
import im.bigs.pg.infra.persistence.partner.repository.FeePolicyJpaRepository
import im.bigs.pg.infra.persistence.partner.repository.PartnerJpaRepository
import im.bigs.pg.infra.persistence.payment.entity.PaymentEntity
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 로컬/데모 환경에서 빠른 실행을 위한 간단한 시드 데이터.
 * - 운영 환경에서는 제거하거나 마이그레이션 도구로 대체합니다.
 */
@Configuration
class DataInitializer {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seed(
        partnerRepo: PartnerJpaRepository,
        feeRepo: FeePolicyJpaRepository,
        paymentRepo: PaymentJpaRepository
    ) = CommandLineRunner {
        if (partnerRepo.count() == 0L) {
            val p1 = partnerRepo.save(PartnerEntity(code = "MOCK1", name = "Mock Partner 1", active = true))
            val p2 = partnerRepo.save(PartnerEntity(code = "TESTPAY1", name = "TestPay Partner 1", active = true))
            feeRepo.save(
                FeePolicyEntity(
                    partnerId = p1.id!!,
                    effectiveFrom = Instant.parse("2020-01-01T00:00:00Z"),
                    percentage = BigDecimal("0.0235"),
                    fixedFee = BigDecimal.ZERO,
                ),
            )
            feeRepo.save(
                FeePolicyEntity(
                    partnerId = p2.id!!,
                    effectiveFrom = Instant.parse("2021-01-01T00:00:00Z"),
                    percentage = BigDecimal("0.0300"),
                    fixedFee = BigDecimal("100"),
                ),
            )
            feeRepo.save(
                FeePolicyEntity(
                    partnerId = p2.id!!,
                    effectiveFrom = Instant.parse("2022-01-01T00:00:00Z"),
                    percentage = BigDecimal("0.0200"),
                    fixedFee = BigDecimal("90"),
                ),
            )
            feeRepo.save(
                FeePolicyEntity(
                    partnerId = p2.id!!,
                    effectiveFrom = Instant.parse("2025-01-01T00:00:00Z"),
                    percentage = BigDecimal("0.0100"),
                    fixedFee = BigDecimal("50"),
                ),
            )

            // 초기 결제 데이터 10개 추가 (시간순 정렬)
            val paymentData = listOf(
                // ID, amount, appliedFeeRate, approvalCode, approvedAt, cardBin, cardLast4, createdAt, feeAmount, netAmount, partnerId, status, updatedAt
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.030000"),
                    approvalCode = "1017952",
                    approvedAt = "2025-10-17 18:52:48.629765",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("400"),
                    netAmount = BigDecimal("9600"),
                    partnerId = p2.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.023500"),
                    approvalCode = "10171574",
                    approvedAt = "2025-10-17 18:53:15.197766",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("235"),
                    netAmount = BigDecimal("9765"),
                    partnerId = p1.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.023500"),
                    approvalCode = "10179924",
                    approvedAt = "2025-10-17 18:54:35.234771",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("235"),
                    netAmount = BigDecimal("9765"),
                    partnerId = p1.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.030000"),
                    approvalCode = "1017954",
                    approvedAt = "2025-10-17 18:54:40.546617",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("400"),
                    netAmount = BigDecimal("9600"),
                    partnerId = p2.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.030000"),
                    approvalCode = "10178926",
                    approvedAt = "2025-10-17 18:59:45.815977",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("400"),
                    netAmount = BigDecimal("9600"),
                    partnerId = p2.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.030000"),
                    approvalCode = "1017100",
                    approvedAt = "2025-10-17 19:00:14.230991",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("400"),
                    netAmount = BigDecimal("9600"),
                    partnerId = p2.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.030000"),
                    approvalCode = "1017100",
                    approvedAt = "2025-10-17 19:00:29.268912",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("400"),
                    netAmount = BigDecimal("9600"),
                    partnerId = p2.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.023500"),
                    approvalCode = "10173349",
                    approvedAt = "2025-10-17 19:00:39.228667",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("235"),
                    netAmount = BigDecimal("9765"),
                    partnerId = p1.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.023500"),
                    approvalCode = "10179582",
                    approvedAt = "2025-10-17 19:02:36.776585",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("235"),
                    netAmount = BigDecimal("9765"),
                    partnerId = p1.id!!,
                    status = PaymentStatus.APPROVED.name
                ),
                createPaymentData(
                    amount = BigDecimal("10000"),
                    appliedFeeRate = BigDecimal("0.023500"),
                    approvalCode = "10173082",
                    approvedAt = "2025-10-17 19:02:57.091671",
                    cardBin = "123456",
                    cardLast4 = "4242",
                    feeAmount = BigDecimal("235"),
                    netAmount = BigDecimal("9765"),
                    partnerId = p1.id!!,
                    status = PaymentStatus.APPROVED.name
                )
            )

            // 데이터 저장
            paymentData.forEach {
                paymentRepo.save(it)
            }

            log.info("Seeded partners: {} and {}", p1.id, p2.id)
            log.info("Seeded {} sample payment records", paymentData.size)
        }
    }

    private fun createPaymentData(
        amount: BigDecimal,
        appliedFeeRate: BigDecimal,
        approvalCode: String,
        approvedAt: String,
        cardBin: String,
        cardLast4: String,
        feeAmount: BigDecimal,
        netAmount: BigDecimal,
        partnerId: Long,
        status: String
    ): PaymentEntity {
        val approvedAtInstant = ZonedDateTime.parse(
            approvedAt.replace(" ", "T") + "Z",
            DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC)
        ).toInstant()

        return PaymentEntity(
            amount = amount,
            appliedFeeRate = appliedFeeRate,
            approvalCode = approvalCode,
            approvedAt = approvedAtInstant,
            cardBin = cardBin,
            cardLast4 = cardLast4,
            createdAt = approvedAtInstant, // 생성 시각도 승인 시각과 동일하게 설정
            feeAmount = feeAmount,
            netAmount = netAmount,
            partnerId = partnerId,
            status = status,
            updatedAt = approvedAtInstant // 업데이트 시각도 승인 시각과 동일하게 설정
        )
    }
}
