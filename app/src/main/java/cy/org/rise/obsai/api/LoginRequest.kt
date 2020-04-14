package cy.org.rise.obsai.api

import com.google.gson.annotations.SerializedName

/**
 * Data class for Retrofit login requests.
 *
 * @property email user email
 * @property password user password
 */
data class LoginRequest(
    @SerializedName("email")
    var email: String,

    @SerializedName("password")
    var password: String
)