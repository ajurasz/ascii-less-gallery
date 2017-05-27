package ajurasz.lambda.auth

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.service.UserService
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.FreeSpec
import org.junit.Assert.assertEquals
import java.util.*

class LoginHandlerSpec : FreeSpec() {

    val userServiceMock: UserService = mock()
    val loginHandler = LoginHandler(userServiceMock)

    init {
        "Auth POST LoginHandler" - {
            "Should return 401 when no 'Authorization' header found" {
                // when
                val result = loginHandler.handleRequest(ApiGatewayRequest.Input(), mock())

                // then
                result shouldEqual ApiGatewayResponse(401, mapOf("message" to "Invalid email or password"))
            }

            "Should handle empty 'Authorization' header" {
                // given
                val authorization = "Basic "

                // when
                val result = loginHandler.handleRequest(ApiGatewayRequest.Input(
                        headers = mutableMapOf("Authorization" to authorization)), mock())

                // then
                result shouldEqual ApiGatewayResponse(401, mapOf("message" to "Invalid email or password"))
            }

            "Should handle invalid email and/or password" {
                // given
                val authorization = "Basic foobar"

                // when
                whenever(userServiceMock.login(any(), any(), any())).thenReturn(null)
                val result = loginHandler.handleRequest(ApiGatewayRequest.Input(
                        headers = mutableMapOf("Authorization" to authorization)), mock())

                // then
                result shouldEqual ApiGatewayResponse(401, mapOf("message" to "Invalid email or password"))
            }

            "Should handle invalid email and/or password" {
                // given
                val authorization = "Basic foobar"

                // when
                whenever(userServiceMock.login(any(), any(), any())).thenReturn(null)
                val result = loginHandler.handleRequest(ApiGatewayRequest.Input(
                        headers = mutableMapOf("Authorization" to authorization)), mock())

                // then
                result shouldEqual ApiGatewayResponse(401, mapOf("message" to "Invalid email or password"))
            }

            "Should return token after successfully login" {
                // given
                val email = "foo"
                val password = "bar"
                val token = UUID.randomUUID().toString()
                val authorization = "Basic ${Base64.getEncoder().encodeToString("$email:$password".toByteArray())}"

                // when
                whenever(userServiceMock.login(eq(email), eq(password), isNull())).thenReturn(token)
                val result = loginHandler.handleRequest(ApiGatewayRequest.Input(
                        headers = mutableMapOf("Authorization" to authorization)), mock())

                // then
                verify(userServiceMock, times(1)).login(
                        check { assertEquals(email, it) },
                        check { assertEquals(password, it) },
                        isNull())
                result shouldEqual ApiGatewayResponse(200, mapOf("token" to token))
            }
        }
    }
}