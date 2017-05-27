package ajurasz.lambda.service

import ajurasz.model.User
import ajurasz.service.CacheService
import ajurasz.service.JWTService
import ajurasz.service.UserService
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.nhaarman.mockito_kotlin.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.FreeSpec
import org.junit.Assert
import org.junit.Assert.assertEquals

class UserServiceSpec : FreeSpec() {
    val cacheServiceMock: CacheService = mock()
    val jwtServiceMock: JWTService = mock()
    val dynamoDBMock: DynamoDB = mock()
    val tableMock: Table = mock()
    init {

        "User service" - {
            whenever(dynamoDBMock.getTable(any())).thenReturn(tableMock)
            val userService = UserService(dynamoDBMock, cacheServiceMock, jwtServiceMock)
            "Should create user" {
                // given
                val user = User("foo@bar.com", "password")

                // when
                userService.create(user)

                // then
                verify(tableMock).putItem(check<Item> {
                    assertEquals(it.get(UserService.Field.EMAIL.value), user.email)
                    assertEquals(it.get(UserService.Field.PASSWORD.value), user.password)
                })
            }

            "Should load existing user" {
                // given
                val user = User("foo@bar.com", "password")

                // when
                whenever(tableMock.getItem(any(), eq(user.email), any(), eq(null)))
                        .thenReturn(Item()
                                .withPrimaryKey(UserService.Field.EMAIL.value, user.email)
                                .withString(UserService.Field.PASSWORD.value, user.password))
                val result = userService.load(user.email)

                // then
                result shouldNotBe null
                result!!.email shouldEqual user.email
            }

            "Should return null when user not found" {
                // given
                val user = User("foo@bar.com", "password")

                // when
                whenever(tableMock.getItem(any(), eq(user.email), any(), eq(null)))
                        .thenReturn(null)
                val result = userService.load(user.email)

                // then
                result shouldEqual null
            }

            "Should return token for valid user" {
                // given
                val user = User("foo@bar.com", "password")
                val token = "token"

                // when
                whenever(jwtServiceMock.create(eq(user.email))).thenReturn(token)
                whenever(tableMock.getItem(any(), eq(user.email), any(), eq(null)))
                        .thenReturn(Item()
                                .withPrimaryKey(UserService.Field.EMAIL.value, user.email)
                                .withString(UserService.Field.PASSWORD.value, "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"))
                val result = userService.login(user.email, user.password)

                // then
                result shouldEqual token
                verify(cacheServiceMock, times(1)).set(check { assertEquals(it, token) }, any(), any())
                verify(cacheServiceMock, times(0)).del(any())
            }

            "Should replace existing token for valid user" {
                // given
                val user = User("foo@bar.com", "password")
                val token = "token"
                val oldToken = "old token"

                // when
                whenever(jwtServiceMock.create(eq(user.email))).thenReturn(token)
                whenever(tableMock.getItem(any(), eq(user.email), any(), eq(null)))
                        .thenReturn(Item()
                                .withPrimaryKey(UserService.Field.EMAIL.value, user.email)
                                .withString(UserService.Field.PASSWORD.value, "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"))
                val result = userService.login(user.email, user.password, oldToken)

                // then
                result shouldEqual token
                verify(cacheServiceMock, times(1)).set(check { assertEquals(it, token) }, any(), any())
                verify(cacheServiceMock, times(1)).del(check { assertEquals(it, oldToken) })
            }

            "Should not return token for invalid password" {
                // given
                val user = User("foo@bar.com", "password")

                // when
                whenever(tableMock.getItem(any(), eq(user.email), any(), eq(null)))
                        .thenReturn(Item()
                                .withPrimaryKey(UserService.Field.EMAIL.value, user.email)
                                .withString(UserService.Field.PASSWORD.value, "invalid password"))
                val result = userService.login(user.email, user.password)

                // then
                result shouldBe null
                verify(cacheServiceMock, times(0)).set(any(), any(), any())
                verify(cacheServiceMock, times(0)).del(any())
            }

            "Should not return token for not existing user" {
                // given
                val user = User("foo@bar.com", "password")

                // when
                whenever(tableMock.getItem(any(), eq(user.email), any(), eq(null)))
                        .thenReturn(null)
                val result = userService.login(user.email, user.password)

                // then
                result shouldBe null
                verify(cacheServiceMock, times(0)).set(any(), any(), any())
                verify(cacheServiceMock, times(0)).del(any())
            }

            "Should found user by token" {
                // given
                val token = "token"
                val user = User("foo@bar.com", "password")

                // when
                whenever(cacheServiceMock.get(eq(token), eq(User::class.java))).thenReturn(user)
                val result = userService.findUserByToken(token)

                // then
                result shouldEqual user
                verify(cacheServiceMock, times(1)).set(any(), any(), any())
            }

            "Should not found user by token" {
                // given
                val token = "token"
                val user = User("foo@bar.com", "password")

                // when
                whenever(cacheServiceMock.get(eq(token), eq(User::class.java))).thenReturn(null)
                val result = userService.findUserByToken(token)

                // then
                result shouldBe null
                verify(cacheServiceMock, times(0)).set(any(), any(), any())
            }
        }
    }
}