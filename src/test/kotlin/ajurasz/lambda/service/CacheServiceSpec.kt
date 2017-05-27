package ajurasz.lambda.service

import ajurasz.model.User
import ajurasz.service.CacheService
import ajurasz.toJson
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.FreeSpec
import org.junit.Assert.assertEquals
import redis.clients.jedis.Jedis

class CacheServiceSpec: FreeSpec() {
    val jedisMock: Jedis = mock()
    val cacheService = CacheService(jedisMock)
    init {
        "Cache service" - {
            "Should set key" {
                // given
                val key = "foo"
                val exp = 60
                val user = User("foo@bar.com", "password")

                // when
                cacheService.set(key, exp, user)

                // then
                verify(jedisMock, times(1))
            }
            
            "Should get existing key" {
                // given
                val key = "foo"
                val user = User("foo@bar.com", "password")

                // when
                whenever(jedisMock.get(eq(key))).thenReturn(user.toJson())
                val result = cacheService.get(key, User::class.java)
                
                // then
                result shouldEqual user
            }

            "Should return null for not existing key" {
                // given
                val key = "foo"
                val obj = "token"

                // when
                whenever(jedisMock.get(eq(key))).thenReturn(null)
                val result = cacheService.get(key, String::class.java)

                // then
                result shouldBe null
            }

            "Should delete key" {
                // given
                val key = "foo"

                // when
                cacheService.del(key)

                // then
                verify(jedisMock).del(
                        check<String> { assertEquals(it, key) })
            }
        }
    }
}