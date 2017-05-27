package ajurasz.lambda.auth

import ajurasz.lambda.ApiGatewayRequest
import ajurasz.lambda.ApiGatewayResponse
import ajurasz.model.User
import ajurasz.service.UserService
import ajurasz.sha256Hex
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.commons.lang3.BooleanUtils
import org.apache.log4j.Logger

class RegisterHandler(private val userService: UserService)
    : RequestHandler<ApiGatewayRequest.Input, ApiGatewayResponse> {

    constructor() : this(UserService())

    override fun handleRequest(input: ApiGatewayRequest.Input, context: Context): ApiGatewayResponse {
        LOG.debug("input(\n$input\n)")
        val data = input.bodyToObject(RegisterRequest::class.java)
        data?.let {
            if (BooleanUtils.isTrue(data.validate())) {
                if (userService.load(data.email!!) != null)
                    return ApiGatewayResponse(400, mapOf("message" to "Email address exist"))
                userService.create(User(data.email, data.password!!.sha256Hex()))
                return ApiGatewayResponse()
            }
        }

        return ApiGatewayResponse(400, mapOf("message" to "Invalid request body"))
    }

    data class RegisterRequest(val email: String? = null, val password: String? = null) {
        constructor() : this(null, null) // Required by Jackson
        fun validate() = email != null && password != null && email.isNotBlank() && password.isNotBlank()
    }

    companion object {
        val LOG: Logger = Logger.getLogger(RegisterHandler::class.java)
    }
}