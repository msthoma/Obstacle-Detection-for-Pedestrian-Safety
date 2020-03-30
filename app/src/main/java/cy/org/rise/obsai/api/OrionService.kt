package cy.org.rise.obsai.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OrionService {
    @GET("version")
    fun getVersion(): Call<OrionVersion>

    @POST("v2/entities")
    suspend fun insertObstacle(@Body restObstacle: RestObstacle): Response<Unit>
}