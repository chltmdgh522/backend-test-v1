package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryPaymentsUseCase
import im.bigs.pg.application.payment.port.`in`.QueryResult
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.domain.payment.PaymentSummary
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64

/**
 * 결제 이력 조회 유스케이스 구현체.
 * - 커서 토큰은 createdAt/id를 안전하게 인코딩해 전달/복원합니다.
 * - 통계는 조회 조건과 동일한 집합을 대상으로 계산됩니다.
 */
@Service
class QueryPaymentsService(
    private val paymentOutPort: PaymentOutPort
) : QueryPaymentsUseCase {
    /**
     * 필터를 기반으로 결제 내역을 조회합니다.
     *
     * @param filter 파트너/상태/기간/커서/페이지 크기
     * @return 조회 결과(목록/통계/커서)
     */
    override fun query(filter: QueryFilter): QueryResult {
        // 1. 커서 디코딩
        val (cursorInstant, cursorId) = decodeCursor(filter.cursor)
        val cursorCreatedAt = cursorInstant?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }

        // 2. PaymentQuery 객체 생성 (커서와 필터 조건 적용)
        val paymentStatus = filter.status?.let {
            try {
                PaymentStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        val query = PaymentQuery(
            partnerId = filter.partnerId,
            status = paymentStatus,
            from = filter.from,
            to = filter.to,
            limit = filter.limit,
            cursorCreatedAt = cursorCreatedAt,
            cursorId = cursorId
        )

        // 3. 페이지네이션 결과 조회
        val page = paymentOutPort.findBy(query)

        // 4. 통계 조회 (동일한 필터 조건으로)
        val summaryFilter = PaymentSummaryFilter(
            partnerId = filter.partnerId,
            status = paymentStatus,
            from = filter.from,
            to = filter.to
        )
        val summaryProjection = paymentOutPort.summary(summaryFilter)
        val summary = PaymentSummary(
            count = summaryProjection.count,
            totalAmount = summaryProjection.totalAmount,
            totalNetAmount = summaryProjection.totalNetAmount
        )

        // 5. 다음 페이지 커서 생성
        val nextCursor = if (page.hasNext && page.nextCursorCreatedAt != null && page.nextCursorId != null) {
            encodeCursor(page.nextCursorCreatedAt.toInstant(ZoneOffset.UTC), page.nextCursorId)
        } else {
            null
        }

        // 6. QueryResult 반환
        return QueryResult(
            items = page.items,
            summary = summary,
            nextCursor = nextCursor,
            hasNext = page.hasNext
        )
    }

    /**
     * 커서 인코딩과 디코딩 간단 설명
     *
     * 인코딩: 데이터베이스 조회 위치(생성시간+ID)를 문자열로 변환한 후 Base64로 암호화해서 URL에서 안전하게 전달할 수 있는 형태로 만듭니다.
     * 디코딩: 받은 Base64 암호화 문자열을 원래 형태(생성시간+ID)로 복원해서 다음 데이터베이스 쿼리의 시작점으로 활용합니다.
     * 목적: 페이지 번호 대신 마지막으로 본 항목 정보를 사용해 데이터가 추가되거나 삭제되어도 일관된 페이지 탐색이 가능합니다.
     * 장점: 데이터 누락이나 중복 없이 대용량 데이터를 효율적으로 탐색할 수 있으며, 데이터베이스 인덱스를 효과적으로 활용해 성능이 우수합니다.
     *
     * */

    /** 다음 페이지 이동을 위한 커서 인코딩. */
    private fun encodeCursor(createdAt: Instant?, id: Long?): String? {
        if (createdAt == null || id == null) return null
        val raw = "${createdAt.toEpochMilli()}:$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())
    }

    /** 요청으로 전달된 커서 복원. 유효하지 않으면 null 커서로 간주합니다. */
    private fun decodeCursor(cursor: String?): Pair<Instant?, Long?> {
        if (cursor.isNullOrBlank()) return null to null
        return try {
            val raw = String(Base64.getUrlDecoder().decode(cursor))
            val parts = raw.split(":")
            val ts = parts[0].toLong()
            val id = parts[1].toLong()
            Instant.ofEpochMilli(ts) to id
        } catch (e: Exception) {
            null to null
        }
    }
}
