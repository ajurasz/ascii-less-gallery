package ajurasz.lambda.auth

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.model.User
import ajurasz.service.UserService
import ajurasz.sha256Hex
import ajurasz.toJson
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.FreeSpec
import org.junit.Assert.assertEquals

class RegisterHandlerSpec : FreeSpec() {

    val userServiceMock: UserService = mock()
    val registerHandler = RegisterHandler(userServiceMock)

    init {
        "Auth POST RegisterHandler" - {
            "Should return 400 for invalid request body" {
                // when
                val result = registerHandler.handleRequest(ApiGatewayRequest.Input(), mock())

                // then
                result shouldEqual ApiGatewayResponse(400, mapOf("message" to "Invalid request body"))
            }

            "Should return 400 for empty email and password fields" {
                // given
                val body = mapOf("email" to "", "password" to "").toJson()

                // when
                val result = registerHandler.handleRequest(ApiGatewayRequest.Input(body = body), mock())

                // then
                result shouldEqual ApiGatewayResponse(400, mapOf("message" to "Invalid request body"))
            }

            "Should return 400 for existing email address" {
                // given
                val body = mapOf("email" to "foo", "password" to "bar").toJson()

                // when
                whenever(userServiceMock.load(any())).thenReturn(User("foo", "bar"))
                val result = registerHandler.handleRequest(ApiGatewayRequest.Input(body = body), mock())

                // then
                result shouldEqual ApiGatewayResponse(400, mapOf("message" to "Email address exist"))
            }

            "Should create new user" {
                // given
                val email = "foo"
                val password = "bar"
                val body = mapOf("email" to email, "password" to password).toJson()

                // when
                whenever(userServiceMock.load(any())).thenReturn(null)
                val result = registerHandler.handleRequest(ApiGatewayRequest.Input(body = body), mock())

                // then
                verify(userServiceMock, times(1)).create(check {
                    assertEquals(it.email, email)
                    assertEquals(it.password, password.sha256Hex())
                })
                result shouldEqual ApiGatewayResponse(200)
            }
        }
    }
}