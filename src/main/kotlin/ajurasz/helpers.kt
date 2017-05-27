package ajurasz

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import java.util.*

inline fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun customGson() = GsonBuilder()
        .registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, typeOfT, context ->
            Date(json.asJsonPrimitive.asLong)
        })
        .create()