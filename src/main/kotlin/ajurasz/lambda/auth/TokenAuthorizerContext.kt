package ajurasz.lambda.auth

data class TokenAuthorizerContext(var type: String? = null,
                             var authorizationToken: String? = null,
                             var methodArn: String? = null)