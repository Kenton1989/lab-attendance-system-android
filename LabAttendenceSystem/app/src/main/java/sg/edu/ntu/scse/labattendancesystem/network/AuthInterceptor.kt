package sg.edu.ntu.scse.labattendancesystem.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val getToken: () -> String?, private val authHeaderPrefix: String = "Token"): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // If token has been saved, add it to the request
        getToken()?.let {
            val requestBuilder = request.newBuilder()
            requestBuilder.addHeader("Authorization", "$authHeaderPrefix $it")
            request = requestBuilder.build()
        }

        return chain.proceed(request)
    }
}