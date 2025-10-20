package im.bigs.pg.api.common.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 응답 정보")
data class ErrorResponse(
    @Schema(description = "발생 시각", example = "2025-10-20T13:27:50.029+00:00")
    val timestamp: String,

    @Schema(description = "HTTP 상태 코드", example = "500")
    val status: Int,

    @Schema(description = "에러 요약", example = "Bad Request")
    val error: String,

    @Schema(description = "에러 메시지", example = "Invalid payment amount")
    val message: String?,

    @Schema(description = "요청 경로", example = "/api/v1/payments")
    val path: String
)
