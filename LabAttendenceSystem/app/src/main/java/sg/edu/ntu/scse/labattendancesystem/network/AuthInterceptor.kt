package sg.edu.ntu.scse.labattendancesystem.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authHeaderPrefix: String = "Token", private val getToken: () -> String?): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // If token has been saved, add it to the request
        getToken()?.let {
            requestBuilder.addHeader("Authorization", "$authHeaderPrefix $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}