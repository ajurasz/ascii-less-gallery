package ajurasz.lambda.gallery

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.safeLet
import ajurasz.service.ElasticsearchService
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.log4j.Logger

class DeleteHandler(private val esService: ElasticsearchService)
    : RequestHandler<ApiGatewayRequest.Input, ApiGatewayResponse> {

    constructor() : this(ElasticsearchService())

    override fun handleRequest(input: ApiGatewayRequest.Input, context: Context): ApiGatewayResponse {
        CreateHandler.LOG.debug("input(\n$input\n)")
        safeLet(input.pathParameters?.get("id"), input.requestContext?.authorizer?.principalId) { id, email ->
            when (esService.remove(id, email)) {
                true -> return ApiGatewayResponse()
                false -> return ApiGatewayResponse(404, mapOf("message" to "Not found"))
            }
        }

        return ApiGatewayResponse(400, mapOf("message" to "Invalid request"))
    }

    companion object {
        val LOG: Logger = Logger.getLogger(DeleteHandler::class.java)
    }
}