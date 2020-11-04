package cy.org.rise.obsai.api

import android.util.Log
import com.google.gson.GsonBuilder
import cy.org.rise.obsai.db.Obstacle
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/** Interface for Retrofit requests. */
interface INicosiaApi {
    @GET("get_obstacles_problems")
    suspend fun getAllServerObstacles(): List<Obstacle>

    @FormUrlEncoded
    @POST("oauth2/token")
    @Headers("No-AccessToken-Required: true")
    suspend fun login(
        @Header("Authorization") authorization: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String
    ): Response<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("post_obstacles_problems")
    fun postToiNicosiaJson(@Body obstacle: Obstacle): Call<Unit>

    companion object {
        /** Keyrock endpoint, used for authentication. */
        const val LOGIN_BASE_URL = "http://192.168.10.10:3005/"

        /** iNicosia endpoint. */
        const val iNICOSIA_BASE_URL = "http://inicosia.rise.org.cy/docs/"

        /**
         * Allows for singleton instantiation of the Retrofit service. Based on this
         * [example](https://git.io/JJ0RI).
         *
         * @param baseURL
         * @param accessToken
         * @return iNicosia API
         */
        fun create(baseURL: String, accessToken: String = ""): INicosiaApi? = baseURL
            .toHttpUrlOrNull()?.let { create(it, accessToken) }

        private fun create(httpUrl: HttpUrl, accessToken: String = ""): INicosiaApi {
            // add logger to Retrofit
            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("API", message)
                }
            })
            logger.level = HttpLoggingInterceptor.Level.BODY

            val serviceInterceptor = ServiceInterceptor()
            serviceInterceptor.token = accessToken

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(serviceInterceptor)
                .build()

            // TODO 04/08/20 try to use custom serializer for location as shown
            //  here https://stackoverflow.com/questions/6873020/gson-date-format
//            val des = JsonSerializer<Obstacle.Location>()

//            val ser: JsonSerializer<Date> =
//                JsonSerializer<Any?> { src, typeOfSrc, context ->
//                    if (src == null) null else JsonPrimitive(
//                        src.getTime()
//                    )
//                }

            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        // make sure only exposed fields are JSONified
                        GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
                            .create()
                    )
                )
                .build()
                .create(INicosiaApi::class.java)
        }
    }
}
