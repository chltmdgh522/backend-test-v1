package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Schema(description = "결제 생성 요청")
data class CreatePaymentRequest(
    @field:NotNull
    @field:Schema(description = "제휴사 ID", example = "1", required = true)
    val partnerId: Long,

    @field:NotNull
    @field:Min(1)
    @field:Schema(description = "결제 금액(정수값)", example = "10000", required = true)
    val amount: BigDecimal,

    @field:Schema(description = "카드 BIN 번호(선택)", example = "123456")
    val cardBin: String? = null,

    @field:Schema(description = "카드 마지막 4자리(선택)", example = "4242")
    val cardLast4: String? = null,

    @field:Schema(description = "상품명(선택)", example = "프리미엄 구독")
    val productName: String? = null,
)
