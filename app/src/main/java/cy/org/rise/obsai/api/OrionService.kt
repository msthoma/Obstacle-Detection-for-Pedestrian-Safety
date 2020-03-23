package cy.org.rise.obsai.api

import retrofit2.Call
import retrofit2.http.GET

interface OrionService {
    @GET("version")
    fun getVersion(): Call<OrionVersion>
}