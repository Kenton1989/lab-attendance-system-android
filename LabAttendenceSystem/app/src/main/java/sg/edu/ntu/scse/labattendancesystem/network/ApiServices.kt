package sg.edu.ntu.scse.labattendancesystem.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import sg.edu.ntu.scse.labattendancesystem.network.api.AuthApi
import java.util.Date


class ApiServices() {
    companion object {
        val token: AuthApi by lazy { buildRetrofit().create(AuthApi::class.java) }

        private const val baseUrl = "http://172.21.148.198"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

        private fun buildRetrofit() =
            Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(baseUrl).build()
    }

}