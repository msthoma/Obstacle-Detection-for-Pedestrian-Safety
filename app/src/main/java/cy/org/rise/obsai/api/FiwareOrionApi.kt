package cy.org.rise.obsai.api

import cy.org.rise.obsai.db.Obstacle
import retrofit2.Call
import retrofit2.Response
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
}