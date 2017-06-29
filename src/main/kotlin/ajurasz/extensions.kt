package ajurasz

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.codec.digest.DigestUtils

val mapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun Any.toJson(): String = mapper.writeValueAsString(this)
fun <T> String.toObject(clazz: Class<T>): T = mapper.readValue(this, clazz)

fun String.sha256Hex() = DigestUtils.sha256Hex(this)