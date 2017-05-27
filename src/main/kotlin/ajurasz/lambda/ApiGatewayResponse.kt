package ajurasz.lambda

import ajurasz.toJson

data class ApiGatewayResponse(val statusCode: Int, val body: String, val headers: Map<String, String>) {
    constructor() : this(200)
    constructor(statusCode: Int) : this(statusCode, "", hashMapOf())
    constructor(body: Any) : this(200, body.toJson(), hashMapOf())
    constructor(statusCode: Int, body: Any) : this(statusCode, body.toJson(), hashMapOf())
    constructor(statusCode: Int, body: Any, headers: Map<String, String>) : this(statusCode, body.toJson(), headers)
}