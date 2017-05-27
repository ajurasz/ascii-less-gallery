package ajurasz.service

import ajurasz.model.User
import ajurasz.sha256Hex
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import org.apache.log4j.Logger

class UserService {

    private val table: Table
    private val cache: CacheService
    private val jwtService: JWTService

    constructor() {
        this.cache = CacheService()
        this.jwtService = JWTService()

        val client = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()

        this.table = DynamoDB(client).getTable(TABLE_NAME)
    }

    constructor(dynamoDB: DynamoDB, cache: CacheService, jwtService: JWTService) {
        this.cache = cache
        this.jwtService = jwtService
        this.table = dynamoDB.getTable(TABLE_NAME)
    }

    enum class Field(val value: String) {
        EMAIL("email"),
        PASSWORD("password")
    }

    fun create(user: User) {
        LOG.debug("Create $user")
        table.putItem(Item()
                .withPrimaryKey(Field.EMAIL.value, user.email)
                .withString(Field.PASSWORD.value, user.password))
    }

    fun load(email: String): User? {
        LOG.debug("Load user for $email")
        val item = table.getItem(Field.EMAIL.value, email,
                "${Field.EMAIL.value}, ${Field.PASSWORD.value}", null) ?: return null
        return User(item.getString(Field.EMAIL.value), item.getString(Field.PASSWORD.value))
    }

    fun login(email: String, password: String): String? {
        return login(email, password, null)
    }

    fun login(email: String, password: String, existingToken: String?): String? {
        val user = load(email)
        user?.let {
            existingToken?.let { cache.del(existingToken) }
            if (user.password == password.sha256Hex()) {
                val token = jwtService.create(user.email)
                cache.set(token, 3600, user)
                return token
            }
        }

        return null
    }

    fun findUserByToken(token: String): User? {
        LOG.info("Find token $token")
        val user = cache.get(token, User::class.java)
        user?.let {
            LOG.info("Token found")
            cache.set(token, 3600, user) // reset token expiration
            return user
        }
        LOG.info("Token not found")
        return null
    }

    companion object {
        val LOG: Logger = Logger.getLogger(UserService::class.java)
        val TABLE_NAME = System.getenv("DYNAMODB_USER_TABLE")
    }
}