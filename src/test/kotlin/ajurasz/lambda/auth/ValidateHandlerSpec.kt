package ajurasz.lambda.auth

import ajurasz.model.User
import ajurasz.service.JWTService
import ajurasz.service.UserService
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.AuthPolicy
import io.AuthPolicy.RESOURCE
import io.AuthPolicy.STATEMENT
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FreeSpec
import java.util.*

class ValidateHandlerSpec : FreeSpec() {

    val userServiceMock: UserService = mock()
    val jwtService = JWTService()
    val validateHandler = ValidateHandler(userServiceMock)

    init {
        "Auth POST ValidateHandler" - {
            "Should return Unauthorized for missing authorization token" {
                // when
                val ex = shouldThrow<RuntimeException> {
                    validateHandler.handleRequest(TokenAuthorizerContext(), mock())
                }

                // then
                ex.message shouldBe "Unauthorized"
            }

            "Should return Unauthorized when token not found" {
                // given
                val token = UUID.randomUUID().toString()

                // when
                whenever(userServiceMock.findUserByToken(eq(token))).thenReturn(null)
                val ex = shouldThrow<RuntimeException> {
                    validateHandler.handleRequest(TokenAuthorizerContext("", token, ""), mock())
                }

                // then
                ex.message shouldBe "Unauthorized"
            }

            "Should return allow policy" {
                // given
                val email = "test@test.com"
                val token = jwtService.create(email)
                val region = "us-east-1"
                val awsAccountId = "*"
                val restApiId = "123"
                val stage = "test"
                val resource = "arn:aws:execute-api:$region:$awsAccountId:$restApiId/$stage/POST/gallery"

                // when
                whenever(userServiceMock.findUserByToken(eq(token))).thenReturn(User(email, "foo"))
                val result = validateHandler.handleRequest(TokenAuthorizerContext("", token, resource), mock())

                // then
                val expectedPolicy = AuthPolicy(email, AuthPolicy.PolicyDocument.getAllowAllPolicy(region, awsAccountId, restApiId, stage))
                result.principalId shouldEqual expectedPolicy.principalId
                @Suppress("UNCHECKED_CAST")
                ((result.policyDocument[STATEMENT] as Array<Map<String, Any>>)[0][RESOURCE] as Array<String>)[0] shouldEqual
                        ((expectedPolicy.policyDocument[STATEMENT] as Array<Map<String, Any>>)[0][RESOURCE] as Array<String>)[0]
            }
        }
    }
}