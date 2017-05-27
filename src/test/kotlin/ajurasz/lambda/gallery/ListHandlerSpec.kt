package ajurasz.lambda.gallery

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.model.GalleryItem
import ajurasz.service.ElasticsearchService
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.FreeSpec

class ListHandlerSpec : FreeSpec() {

    val esServiceMock: ElasticsearchService = mock()
    val listHandler = ListHandler(esServiceMock)

    init {
        "Gallery GET ListHandler" - {
            "Should return response" {
                // when
                whenever(esServiceMock.list()).thenReturn(emptyList())
                val result = listHandler.handleRequest(ApiGatewayRequest.Input(), mock())

                // then
                result shouldEqual ApiGatewayResponse(200, mapOf("results" to emptyList<GalleryItem>()))
            }
        }
    }
}