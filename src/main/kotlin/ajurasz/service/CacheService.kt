package ajurasz.service

import ajurasz.toJson
import ajurasz.toObject
import redis.clients.jedis.Jedis

class CacheService(private val client: Jedis) {

    constructor(): this(Jedis(System.getenv("REDIS_URL"), System.getenv("REDIS_PORT").toInt()))

    fun set(key: String, exp: Int, obj: Any) {
        client.setex(key, exp, obj.toJson())
    }

    fun <T> get(key: String, clazz: Class<T>): T? {
        val obj = client.get(key)
        obj?.let {
            return obj.toObject(clazz)
        }
        return null
    }

    fun del(key: String) {
        client.del(key)
    }
}