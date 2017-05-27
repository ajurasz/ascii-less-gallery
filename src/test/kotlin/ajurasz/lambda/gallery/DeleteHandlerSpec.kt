package ajurasz.lambda.gallery

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.service.ElasticsearchService
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class DeleteHandlerSpec : FreeSpec() {

    val elasticSearchServiceMock: ElasticsearchService = mock()
    val deleteHandler = DeleteHandler(elasticSearchServiceMock)

    init {
        "Gallery DELETE DeleteHandler" -{
            "Should return 400 when id param not passed" {
                // when
                val result = deleteHandler.handleRequest(ApiGatewayRequest.Input(), mock())

                // then
                result shouldBe ApiGatewayResponse(400, mapOf("message" to "Invalid request"))
            }

            "Should return 400 when principalId not found" {
                // when
                val result = deleteHandler.handleRequest(
                        ApiGatewayRequest.Input(pathParameters = mutableMapOf("id" to "10")), mock())

                // then
                result shouldBe ApiGatewayResponse(400, mapOf("message" to "Invalid request"))
            }

            "Should return 404 when document not found" {
                // given
                val id = "foo"
                val email = "bar"

                // when
                whenever(elasticSearchServiceMock.remove(eq(id), eq(email))).thenReturn(false)
                val result = deleteHandler.handleRequest(
                        ApiGatewayRequest.Input(pathParameters = mutableMapOf("id" to id),
                                requestContext = ApiGatewayRequest.RequestContext(
                                        authorizer = ApiGatewayRequest.Authorizer(email)
                                )), mock())

                // then
                result shouldBe ApiGatewayResponse(404, mapOf("message" to "Not found"))
            }

            "Should remove document" {
                // given
                val id = "foo"
                val email = "bar"

                // when
                whenever(elasticSearchServiceMock.remove(eq(id), eq(email))).thenReturn(true)
                val result = deleteHandler.handleRequest(
                        ApiGatewayRequest.Input(pathParameters = mutableMapOf("id" to id),
                                requestContext = ApiGatewayRequest.RequestContext(
                                        authorizer = ApiGatewayRequest.Authorizer(email)
                                )), mock())

                // then
                result shouldBe ApiGatewayResponse()
            }
        }
    }
}