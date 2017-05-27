package ajurasz.lambda.auth

import ajurasz.service.JWTService
import ajurasz.service.UserService
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.AuthPolicy
import org.apache.log4j.Logger


class ValidateHandler(private val userService: UserService) : RequestHandler<TokenAuthorizerContext, AuthPolicy> {

    constructor() : this(UserService())

    override fun handleRequest(input: TokenAuthorizerContext, context: Context): AuthPolicy {
        LOG.debug("input(\n$input\n)")

        val token = when(input.authorizationToken) {
            null -> throw RuntimeException("Unauthorized") // HTTP 401
            else -> input.authorizationToken!!
        }

        return when(userService.findUserByToken(token)) {
            null -> throw RuntimeException("Unauthorized") // HTTP 401
            else -> {
                val principalId = JWTService().getEmail(token)
                generatePolicy(input, principalId)
            }
        }
    }

    private fun generatePolicy(input: TokenAuthorizerContext, principalId: String): AuthPolicy {
        val methodArn = input.methodArn
        val arnPartials = methodArn?.split(":".toRegex())?.dropLastWhile({ it.isEmpty() })?.toTypedArray() ?: emptyArray()
        val region = arnPartials[3]
        val awsAccountId = arnPartials[4]
        val apiGatewayArnPartials = arnPartials[5].split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val restApiId = apiGatewayArnPartials[0]
        val stage = apiGatewayArnPartials[1]
        val httpMethod = apiGatewayArnPartials[2]
        var resource = "" // root resource
        if (apiGatewayArnPartials.size == 4) {
            resource = apiGatewayArnPartials[3]
        }
        return AuthPolicy(principalId, AuthPolicy.PolicyDocument.getAllowAllPolicy(region, awsAccountId, restApiId, stage))
    }

    companion object {
        val LOG: Logger = Logger.getLogger(ValidateHandler::class.java)
    }
}