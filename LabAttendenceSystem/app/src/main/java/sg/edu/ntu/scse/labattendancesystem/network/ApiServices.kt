package sg.edu.ntu.scse.labattendancesystem.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import java.time.LocalDateTime
import java.util.Date
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ApiServices() {
    companion object {
        val token: AuthApi by lazy { buildRetrofit().create(AuthApi::class.java) }

        private const val baseUrl = "http://172.21.148.198/api/v1/"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(LocalDateTime::class.java, LocalDateTimeAdapter().nullSafe())
            .add(LocalDate::class.java, LocalDateAdapter().nullSafe())
            .build()

        private fun buildRetrofit() =
            Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(baseUrl).build()
    }

}

class LocalDateTimeAdapter : JsonAdapter<LocalDateTime>() {
    override fun toJson(writer: JsonWriter, value: LocalDateTime?) {
        value?.let { writer.value(it.format(formatter)) }
    }

    override fun fromJson(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private fun fromNonNullString(nextString: String): LocalDateTime =
        LocalDateTime.parse(nextString, formatter)
}

class LocalDateAdapter : JsonAdapter<LocalDate>() {
    override fun toJson(writer: JsonWriter, value: LocalDate?) {
        value?.let { writer.value(it.format(formatter)) }
    }

    override fun fromJson(reader: JsonReader): LocalDate? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private fun fromNonNullString(nextString: String) =
        LocalDate.parse(nextString, formatter)
}