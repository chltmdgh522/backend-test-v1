package im.bigs.pg.api.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger/OpenAPI 문서화를 위한 설정 클래스.
 */
@Configuration
@OpenAPIDefinition(
    servers = [
        Server(url = "https://flow.madras.p-e.kr", description = "Production Server")
    ]
)
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
    }

    private fun apiInfo() = Info()
        .title("결제 도메인 서버 API")
        .description("나노바나나 페이먼츠 결제 도메인 서버 API 문서")
        .version("v1")
        .contact(
            Contact()
                .name("나노바나나 페이먼츠")
                .url("https://example.com")
                .email("contact@example.com")
        )
}
