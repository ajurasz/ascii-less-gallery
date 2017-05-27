package ajurasz.lambda.service

import ajurasz.service.JWTService
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.FreeSpec

class JWTServiceSpec : FreeSpec() {
    val jwtService = JWTService()
    init {
        "JWT Service" - {
            "Should create token" {
                // given
                val email = "test@test.com"

                // when
                val result = jwtService.create(email)

                // then
                result shouldNotBe null
            }

            "Should decode token" {
                // given
                val email = "test@test.com"

                // when
                val token = jwtService.create(email)
                val result = jwtService.getEmail(token)

                // then
                result shouldBe  email
            }
        }
    }
}