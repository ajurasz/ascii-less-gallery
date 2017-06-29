package ajurasz

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.codec.digest.DigestUtils

val mapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun Any.toJson(): String = mapper.writeValueAsString(this)
inline fun <reified T> String.toObject(): T = mapper.readValue(this, T::class.java)

fun String.sha256Hex() = DigestUtils.sha256Hex(this)