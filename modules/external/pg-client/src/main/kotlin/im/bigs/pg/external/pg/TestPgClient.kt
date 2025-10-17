package im.bigs.pg.external.pg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.payment.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.Cipher
import java.security.MessageDigest
import java.util.Base64

@Component
class TestPgClient(
        private val restTemplate: RestTemplate
) : PgClientOutPort {
    private val log = LoggerFactory.getLogger(javaClass)

    private val baseUrl = "https://api-test-pg.bigs.im"
    private val apiKey = "11111111-1111-4111-8111-111111111111"
    private val iv = "AAAAAAAAAAAAAAAA"

    override fun supports(partnerId: Long): Boolean = partnerId % 2L == 0L

    override fun approve(request: PgApproveRequest): PgApproveResult {

        // 이하 코드는 실행되지 않음
        val url = "$baseUrl/api/v1/pay/credit-card"

        log.info("TestPG 결제 요청: partnerId=${request.partnerId}, amount=${request.amount}")

        try {
            // 카드 번호가 고정이네요..! 422 떠서 요청값에 따라서 못바꿉니다..
            val plainJson = """
                {
                  "cardNumber": "1111-1111-1111-1111",
                  "birthDate": "20000522",
                  "expiry": "1227",
                  "password": "12",
                  "amount": ${request.amount}
                }
            """.trimIndent()

            // 암호화
            val encryptedData = encryptPayload(plainJson, apiKey, iv)

            // 요청 헤더와 본문 구성
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("API-KEY", apiKey)
            }

            val requestBody = mapOf("enc" to encryptedData)
            val entity = HttpEntity(requestBody, headers)

            // API 호출
            val response = restTemplate.postForObject(url, entity, Map::class.java)
                    ?: throw IllegalStateException("No response from TestPg API")

            return PgApproveResult(
                    approvalCode = response["approvalCode"] as String,
                    approvedAt = LocalDateTime.parse(response["approvedAt"] as String),
                    status = PaymentStatus.APPROVED
            )
        } catch (e: Exception) {
            log.error("TestPG API 호출 중 오류: ${e.message}", e)
            throw RuntimeException("TestPG 결제 실패: ${e.message}", e)
        }
    }

    private fun encryptPayload(plaintext: String, apiKey: String, iv: String): String {
        try {
            // 1. API-KEY로 SHA-256 키 생성
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(apiKey.toByteArray(Charsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")

            // 2. IV 디코딩
            val ivBytes = Base64.getUrlDecoder().decode(iv)

            // 3. AES-GCM 암호화
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))

            // 4. 암호화 및 Base64URL 인코딩
            val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted)
        } catch (e: Exception) {
            log.error("암호화 중 오류 발생: ${e.message}", e)
            throw e
        }
    }
}