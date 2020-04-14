package cy.org.rise.obsai.api

import com.google.gson.annotations.SerializedName

/**
 * Data class for Retrofit login responses.
 *
 * @property accessToken token to be used in subsequent connections with server, identifying the
 * user
 * @property expiresIn seconds until [accessToken] expiry
 * @property refreshToken token that can be used to get a new [accessToken] when it expires
 * @property tokenType something like "Bearer" or "Basic", depends on the server
 */
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("expires_in")
    val expiresIn: Int,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("token_type")
    val tokenType: String
)