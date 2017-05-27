package ajurasz.lambda.auth

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.service.UserService
import ajurasz.toJson
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.log4j.Logger
import java.nio.charset.Charset
import java.util.*

class LoginHandler(private val userService: UserService) : RequestHandler<ApiGatewayRequest.Input, ApiGatewayResponse> {

    constructor() : this(UserService())

    override fun handleRequest(input: ApiGatewayRequest.Input, context: Context): ApiGatewayResponse {
        LOG.debug("input(\n$input\n)")
        val authorization = input.headers?.get("Authorization") ?: ""
        val existingToken: String? = input.headers?.get("token")
        val data = extractEmailAndPassword(authorization)

        data?.let {
            val newToken = userService.login(data.first, data.second, existingToken)
            newToken?.let {
                return ApiGatewayResponse(mapOf("token" to newToken))
            }
        }
        return ApiGatewayResponse(401, mapOf("message" to "Invalid email or password"))
    }

    private fun extractEmailAndPassword(authorization: String): Pair<String, String>? {
        if (!authorization.startsWith("Basic")) return null

        val base64Credentials = authorization.substring("Basic".length).trim()
        val credentials = String(Base64.getDecoder().decode(base64Credentials),
                Charset.forName("UTF-8")).split(":")
        if (credentials.size > 1) {
            return Pair(credentials[0], credentials[1])
        } else {
            return null
        }
    }

    companion object {
        val LOG: Logger = Logger.getLogger(LoginHandler::class.java)
    }
}