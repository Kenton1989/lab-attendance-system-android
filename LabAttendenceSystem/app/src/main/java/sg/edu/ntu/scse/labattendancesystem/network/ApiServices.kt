package sg.edu.ntu.scse.labattendancesystem.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import java.time.OffsetDateTime
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import sg.edu.ntu.scse.labattendancesystem.network.api.MainApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ApiServices(
    private val tokenManager: TokenManager,
) {
    val main: MainApi by lazy { buildRetrofitWithAuthentication().create(MainApi::class.java) }

    private fun buildRetrofitWithAuthentication(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(
                getToken = { tokenManager.cachedToken }
            ))
            .build()
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl).build()
    }


    companion object {
        val token: AuthApi by lazy { buildRetrofitWithoutAuthentication().create(AuthApi::class.java) }

        private const val baseUrl = "http://172.21.148.198/api/v1/"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(OffsetDateTime::class.java, OffsetDateTimeAdapter().nullSafe())
            .add(LocalDate::class.java, LocalDateAdapter().nullSafe())
            .build()

        private fun buildRetrofitWithoutAuthentication() =
            Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(baseUrl).build()
    }

}

class OffsetDateTimeAdapter : JsonAdapter<OffsetDateTime>() {
    override fun toJson(writer: JsonWriter, value: OffsetDateTime?) {
        value?.let { writer.value(it.format(formatter)) }
    }

    override fun fromJson(reader: JsonReader): OffsetDateTime? {
        return if (reader.peek() != JsonReader.Token.NULL) {
            fromNonNullString(reader.nextString())
        } else {
            reader.nextNull<Any>()
            null
        }
    }

    private val formatter = DateTimeFormatter.ISO_DATE_TIME
    private fun fromNonNullString(nextString: String): OffsetDateTime =
        OffsetDateTime.parse(nextString, formatter)
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

    private val formatter = DateTimeFormatter.ISO_DATE_TIME
    private fun fromNonNullString(nextString: String) =
        LocalDate.parse(nextString, formatter)
}