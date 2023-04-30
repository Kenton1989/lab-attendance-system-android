package sg.edu.ntu.scse.labattendancesystem.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import sg.edu.ntu.scse.labattendancesystem.network.api.MainApi
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ApiServices(
    private val tokenManager: TokenManager,
) {
    val main: MainApi by lazy { buildRetrofitWithAuthentication().create(MainApi::class.java) }
    val token: AuthApi get() = ApiServices.token

    private fun buildRetrofitWithAuthentication(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(
                getToken = { tokenManager.cachedToken }
            ))
            .build()
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addConverterFactory(RetrofitQueryConverterFactory.create())
            .baseUrl(baseUrl).build()
    }


    companion object {
        val token: AuthApi by lazy { buildRetrofitWithoutAuthentication().create(AuthApi::class.java) }

        private const val baseUrl = "http://172.21.148.198/api/v1/"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(ZonedDateTime::class.java, ZonedDateTimeAdapter().nullSafe())
            .add(LocalDate::class.java, LocalDateAdapter().nullSafe())
            .build()

        private fun buildRetrofitWithoutAuthentication() =
            Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(baseUrl).build()
    }

}

class RetrofitQueryConverterFactory : Converter.Factory() {
    private class ZonedDateTimeQueryConverter : Converter<ZonedDateTime, String> {
        companion object {
            val INSTANCE = ZonedDateTimeQueryConverter()
        }
        override fun convert(t: ZonedDateTime): String? {
            return t.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return if (type === ZonedDateTime::class.java) {
            ZonedDateTimeQueryConverter.INSTANCE
        } else super.stringConverter(type, annotations, retrofit)
    }

    companion object {
        fun create(): RetrofitQueryConverterFactory {
            return RetrofitQueryConverterFactory()
        }
    }
}

class ZonedDateTimeAdapter : JsonAdapter<ZonedDateTime>() {
    override fun toJson(writer: JsonWriter, value: ZonedDateTime?) {
        value?.let { writer.value(it.format(formatter)) }
    }

    override fun fromJson(reader: JsonReader): ZonedDateTime? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }
    }

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private fun fromNonNullString(nextString: String): ZonedDateTime =
        ZonedDateTime.parse(nextString, formatter)
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

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE
    private fun fromNonNullString(nextString: String) =
        LocalDate.parse(nextString, formatter)
}