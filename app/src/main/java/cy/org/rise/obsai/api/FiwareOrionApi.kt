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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FiwareOrionApi {
    @GET("version")
    fun getVersion(): Call<OrionVersion>

    @GET("v2/entities")
    fun getAllObstacles(
        @Query("type") type: String,
        @Query("options") options: String = "keyValues"
    ): Call<List<Obstacle>>

    @POST("v2/entities")
    suspend fun insertObstacle(@Body obstacle: Obstacle): Response<Unit>

    /*
    * Based on this example:
    * https://github.com/android/architecture-components-samples/blob/d81da2cb1e3d61e40f052e631bb15883d0f9f637/PagingWithNetworkSample/app/src/main/java/com/android/example/paging/pagingwithnetwork/reddit/api/RedditApi.kt
    * Essentially allows singleton instantiation of the Retrofit service
    * */
    companion object {
        private const val BASE_URL = "http://192.168.10.10:1026/"

        fun create(): FiwareOrionApi = create(BASE_URL.toHttpUrlOrNull()!!)

        fun create(httpUrl: HttpUrl): FiwareOrionApi {
            // add logger to Retrofit
            val logger = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("API", message)
                }
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    )
                )
                .build()
                .create(FiwareOrionApi::class.java)
        }
    }
}