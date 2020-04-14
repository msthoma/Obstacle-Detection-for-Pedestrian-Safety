package cy.org.rise.obsai.api

import okhttp3.Interceptor

/**
 * Adds an accessToken to the REST request for use in authentication.
 *
 * Token is only added if it's specified, and if it's needed (not needed for logging in for
 * example). Based on [this](https://stackoverflow.com/a/55651256)
 * and [this](https://stackoverflow.com/a/58333111).
 * */

class ServiceInterceptor : Interceptor {

    /**
     * AccessToken for authentication.
     */
    var token: String = ""

    /**
     * Injects an AccessToken (if one is provided) into a Retrofit request that requires
     * authentication.
     *
     * @param chain
     * @return
     */
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()

        if (request.header("No-AccessToken-Required") == null) {
            if (token.isNotEmpty()) {
                request = request.newBuilder().addHeader(
                    "Authorization",
                    "Bearer $token"
                ).build()
            }
        }

        return chain.proceed(request)
    }
}