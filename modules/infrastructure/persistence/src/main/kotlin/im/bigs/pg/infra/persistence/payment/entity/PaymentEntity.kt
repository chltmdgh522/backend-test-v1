package im.bigs.pg.infra.persistence.payment.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

/**
 * DB용 결제 이력 엔티티.
 * - createdAt/Id 조합을 커서 정렬 키로 사용합니다.
 */
@Entity
@Table(
        name = "payment",
        indexes = [
                Index(name = "idx_payment_created", columnList = "created_at DESC, id DESC"),
                Index(name = "idx_payment_partner_created", columnList = "partner_id, created_at DESC")
        ]
)
class PaymentEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Column(nullable = false)
        var partnerId: Long = 0, // 기본값 추가
        @Column(nullable = false, precision = 15, scale = 0)
        var amount: BigDecimal = BigDecimal.ZERO, // 기본값 추가
        @Column(nullable = false, precision = 10, scale = 6)
        var appliedFeeRate: BigDecimal = BigDecimal.ZERO, // 기본값 추가
        @Column(nullable = false, precision = 15, scale = 0)
        var feeAmount: BigDecimal = BigDecimal.ZERO, // 기본값 추가
        @Column(nullable = false, precision = 15, scale = 0)
        var netAmount: BigDecimal = BigDecimal.ZERO, // 기본값 추가
        @Column(length = 8)
        var cardBin: String? = null,
        @Column(length = 4)
        var cardLast4: String? = null,
        @Column(nullable = false, length = 32)
        var approvalCode: String = "", // 기본값 추가
        @Column(nullable = false)
        var approvedAt: Instant = Instant.now(), // 기본값 추가
        @Column(nullable = false, length = 20)
        var status: String = "", // 기본값 추가
        @Column(nullable = false)
        var createdAt: Instant = Instant.now(), // 기본값 추가
        @Column(nullable = false)
        var updatedAt: Instant = Instant.now(), // 기본값 추가
)