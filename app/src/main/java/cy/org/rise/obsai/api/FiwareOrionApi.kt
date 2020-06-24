package cy.org.rise.obsai.api

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Interface for Retrofit requests.
 */
interface FiwareOrionApi {
    @GET("version")
    suspend fun getOrionVersion(): OrionVersion

    @GET("v2/entities")
    suspend fun getAllServerObstacles(@Query("type") type: String): List<RestObstacle>

    @POST("v2/entities")
    suspend fun insertServerObstacle(@Body restObstacle: RestObstacle): Response<Unit>

    @FormUrlEncoded
    @POST("oauth2/token")
    @Headers("No-AccessToken-Required: true")
    suspend fun login(
        @Header("Authorization") authorization: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String
    ): Response<LoginResponse>

    @POST("/post_to_mongo/_id={_id}/type_obs={type_obs}/latitude={latitude}/longitude={longitude}/obstype={obstype}/orie1={orie1}/orie2={orie2}/photopath={photopath}")
    suspend fun postToiNicosia(
        @Path("_id") id: String,
        @Path("type_obs") type_obs: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("obstype") obstype: String,
        @Path("orie1") orie1: Double,
        @Path("orie2") orie2: Double,
        @Path("photopath") photopath: String
    ): Response<Unit>

    companion object {
        /**
         * Keyrock endpoint, used for authentication.
         */
        const val LOGIN_BASE_URL = "http://192.168.10.10:3005/"

        /**
         * Orion Broker endpoint, used for interactions with Fiware.
         */
        const val ORION_BASE_URL = "http://192.168.10.10:1026/"

        /**
         * Allows for singleton instantiation of the Retrofit service.
         *
         * Based on this [example](https://github.com/android/architecture-components-samples/blob/d81da2cb1e3d61e40f052e631bb15883d0f9f637/PagingWithNetworkSample/app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/api/RedditApi.kt).
         *
         * @param baseURL
         * @param accessToken
         * @return
         */
        fun create(baseURL: String, accessToken: String = ""): FiwareOrionApi = create(
            baseURL.toHttpUrlOrNull()!!, accessToken
        )

        private fun create(httpUrl: HttpUrl, accessToken: String = ""): FiwareOrionApi {
            // add logger to Retrofit
            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("API", message)
                }
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val serviceInterceptor = ServiceInterceptor()
            serviceInterceptor.token = accessToken

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(serviceInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
                .create(FiwareOrionApi::class.java)
        }
    }
}