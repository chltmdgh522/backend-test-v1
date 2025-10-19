package im.bigs.pg.infra.persistence.partner.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

/**
 * DB용 수수료 정책 엔티티.
 * - 유효 시작 시점(effectiveFrom) 기준으로 최신 정책을 조회합니다.
 */
@Entity
@Table(
    name = "partner_fee_policy",
    indexes = [
        Index(name = "idx_fee_partner_from", columnList = "partner_id, effective_from DESC")
    ]
)
class FeePolicyEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var partnerId: Long = 0, // 기본값 추가
    @Column(nullable = false)
    var effectiveFrom: Instant = Instant.now(), // 기본값 추가
    @Column(nullable = false, precision = 10, scale = 6)
    var percentage: BigDecimal = BigDecimal.ZERO, // 기본값 추가
    @Column(precision = 15, scale = 0)
    var fixedFee: BigDecimal? = null,
)
