package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueryPaymentsServiceTest {

    private val paymentOutPort = mockk<PaymentOutPort>()
    private val service = QueryPaymentsService(paymentOutPort)

    @Test
    @DisplayName("결제 조회 시 필터 조건이 정확히 적용되어야 한다")
    fun `결제 조회 시 필터 조건이 정확히 적용되어야 한다`() {
        // Given
        val partnerId = 1L
        val status = "APPROVED"
        val from = LocalDateTime.of(2025, 10, 17, 0, 0)
        val to = LocalDateTime.of(2025, 10, 17, 23, 59)
        val limit = 10

        val querySlot = slot<PaymentQuery>()
        val summarySlot = slot<PaymentSummaryFilter>()

        every { paymentOutPort.findBy(capture(querySlot)) } returns PaymentPage(
            items = emptyList(),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )

        every { paymentOutPort.summary(capture(summarySlot)) } returns PaymentSummaryProjection(
            count = 0L,
            totalAmount = BigDecimal.ZERO,
            totalNetAmount = BigDecimal.ZERO
        )

        // When
        service.query(
            QueryFilter(
                partnerId = partnerId,
                status = status,
                from = from,
                to = to,
                cursor = null,
                limit = limit
            )
        )

        // Then
        assertEquals(partnerId, querySlot.captured.partnerId)
        assertEquals(PaymentStatus.APPROVED, querySlot.captured.status)
        assertEquals(from, querySlot.captured.from)
        assertEquals(to, querySlot.captured.to)
        assertEquals(limit, querySlot.captured.limit)
        assertNull(querySlot.captured.cursorCreatedAt)
        assertNull(querySlot.captured.cursorId)

        assertEquals(partnerId, summarySlot.captured.partnerId)
        assertEquals(PaymentStatus.APPROVED, summarySlot.captured.status)
        assertEquals(from, summarySlot.captured.from)
        assertEquals(to, summarySlot.captured.to)
    }

    @Test
    @DisplayName("결과가 없을 때 hasNext는 false이고 nextCursor는 null이어야 한다")
    fun `결과가 없을 때 hasNext는 false이고 nextCursor는 null이어야 한다`() {
        // Given
        every { paymentOutPort.findBy(any()) } returns PaymentPage(
            items = emptyList(),
            hasNext = false,
            nextCursorCreatedAt = null,
            nextCursorId = null
        )

        every { paymentOutPort.summary(any()) } returns PaymentSummaryProjection(
            count = 0L,
            totalAmount = BigDecimal.ZERO,
            totalNetAmount = BigDecimal.ZERO
        )

        // When
        val result = service.query(QueryFilter())

        // Then
        assertFalse(result.hasNext)
        assertNull(result.nextCursor)
        assertTrue(result.items.isEmpty())
        assertEquals(0L, result.summary.count)
        assertEquals(BigDecimal.ZERO, result.summary.totalAmount)
        assertEquals(BigDecimal.ZERO, result.summary.totalNetAmount)
    }

    @Test
    @DisplayName("커서 기반 페이지네이션이 정확하게 동작해야 한다")
    fun `커서 기반 페이지네이션이 정확하게 동작해야 한다`() {
        // Given - 두 페이지에 걸친 테스트 데이터 준비
        val now = LocalDateTime.now()

        // 첫 페이지 데이터
        val page1Items = listOf(
            Payment(
                id = 5L,
                partnerId = 1L,
                amount = BigDecimal("10000"),
                appliedFeeRate = BigDecimal("0.03"),
                feeAmount = BigDecimal("300"),
                netAmount = BigDecimal("9700"),
                cardLast4 = "1111",
                approvalCode = "AP001",
                approvedAt = now.minusMinutes(1),
                status = PaymentStatus.APPROVED,
                createdAt = now.minusMinutes(1),
                updatedAt = now.minusMinutes(1)
            ),
            Payment(
                id = 4L,
                partnerId = 1L,
                amount = BigDecimal("20000"),
                appliedFeeRate = BigDecimal("0.03"),
                feeAmount = BigDecimal("600"),
                netAmount = BigDecimal("19400"),
                cardLast4 = "2222",
                approvalCode = "AP002",
                approvedAt = now.minusMinutes(2),
                status = PaymentStatus.APPROVED,
                createdAt = now.minusMinutes(2),
                updatedAt = now.minusMinutes(2)
            )
        )

        // 두 번째 페이지 데이터
        val page2Items = listOf(
            Payment(
                id = 3L,
                partnerId = 1L,
                amount = BigDecimal("15000"),
                appliedFeeRate = BigDecimal("0.03"),
                feeAmount = BigDecimal("450"),
                netAmount = BigDecimal("14550"),
                cardLast4 = "3333",
                approvalCode = "AP003",
                approvedAt = now.minusMinutes(3),
                status = PaymentStatus.APPROVED,
                createdAt = now.minusMinutes(3),
                updatedAt = now.minusMinutes(3)
            ),
            Payment(
                id = 2L,
                partnerId = 1L,
                amount = BigDecimal("25000"),
                appliedFeeRate = BigDecimal("0.03"),
                feeAmount = BigDecimal("750"),
                netAmount = BigDecimal("24250"),
                cardLast4 = "4444",
                approvalCode = "AP004",
                approvedAt = now.minusMinutes(4),
                status = PaymentStatus.APPROVED,
                createdAt = now.minusMinutes(4),
                updatedAt = now.minusMinutes(4)
            )
        )

        // 첫 페이지 조회 설정
        val firstPageQuery = slot<PaymentQuery>()
        every { paymentOutPort.findBy(capture(firstPageQuery)) } returns PaymentPage(
            items = page1Items,
            hasNext = true,
            nextCursorCreatedAt = page1Items.last().createdAt,
            nextCursorId = page1Items.last().id
        )

        every { paymentOutPort.summary(any()) } returns PaymentSummaryProjection(
            count = 4L, // 전체 결과 개수
            totalAmount = BigDecimal("70000"), // 전체 금액
            totalNetAmount = BigDecimal("67900") // 전체 순수익
        )

        // When - 첫 페이지 조회
        val firstPageResult = service.query(
            QueryFilter(
                partnerId = 1L,
                status = "APPROVED",
                limit = 2
            )
        )

        // Then - 첫 페이지 검증
        assertEquals(2, firstPageResult.items.size)
        assertEquals(5L, firstPageResult.items[0].id) // 첫 번째 항목 ID
        assertEquals(4L, firstPageResult.items[1].id) // 두 번째 항목 ID
        assertTrue(firstPageResult.hasNext) // 다음 페이지 존재
        assertNotNull(firstPageResult.nextCursor) // 다음 페이지 커서 존재

        // 다음 페이지를 위한 설정
        val secondPageQuery = slot<PaymentQuery>()
        every { paymentOutPort.findBy(capture(secondPageQuery)) } returns PaymentPage(
            items = page2Items,
            hasNext = false, // 마지막 페이지
            nextCursorCreatedAt = page2Items.last().createdAt,
            nextCursorId = page2Items.last().id
        )

        // When - 두 번째 페이지 조회 (첫 번째 페이지의 커서 사용)
        val secondPageResult = service.query(
            QueryFilter(
                partnerId = 1L,
                status = "APPROVED",
                limit = 2,
                cursor = firstPageResult.nextCursor
            )
        )

        // Then - 두 번째 페이지 검증
        assertEquals(2, secondPageResult.items.size)
        assertEquals(3L, secondPageResult.items[0].id)
        assertEquals(2L, secondPageResult.items[1].id)
        assertFalse(secondPageResult.hasNext) // 다음 페이지 없음

        // 커서가 올바르게 적용되었는지 검증
        assertNotNull(secondPageQuery.captured.cursorCreatedAt)
        assertEquals(page1Items.last().id, secondPageQuery.captured.cursorId)
    }
}
