package ajurasz.lambda

import ajurasz.toObject

data class ApiGatewayRequest(var input: Input? = null) {

    data class Authorizer(var principalId: String? = null)

    data class RequestContext(var path: String? = null,
                              var accountId: String? = null,
                              var resourceId: String? = null,
                              var stage: String? = null,
                              var authorizer: Authorizer? = null,
                              var requestId: String? = null,
                              var identity: MutableMap<String, String>? = null,
                              var resourcePath: String? = null,
                              var httpMethod: String? = null,
                              var apiId: String? = null)

    data class Input(var resource: String? = null,
                     var path: String? = null,
                     var httpMethod: String? = null,
                     var headers: MutableMap<String, String>? = null,
                     var queryStringParameters: MutableMap<String, String>? = null,
                     var pathParameters: MutableMap<String, String>? = null,
                     var stageVariables: MutableMap<String, String>? = null,
                     var requestContext: RequestContext? = null,
                     var body: String? = null,
                     var isBase64Encoded: Boolean? = null) {
        fun <T> bodyToObject(clazz: Class<T>): T? = body?.toObject(clazz)
    }
}