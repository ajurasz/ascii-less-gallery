package ajurasz.lambda.gallery

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.service.ElasticsearchService
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.log4j.Logger

class ListHandler(private val esService: ElasticsearchService)
    : RequestHandler<ApiGatewayRequest.Input, ApiGatewayResponse> {

    constructor() : this(ElasticsearchService())

    override fun handleRequest(input: ApiGatewayRequest.Input, context: Context): ApiGatewayResponse {
        LOG.debug("input(\n$input\n)")
        return ApiGatewayResponse(mapOf("results" to esService.list()))
    }

    companion object {
        val LOG: Logger = Logger.getLogger(ListHandler::class.java)
    }
}