package cy.org.rise.obsai.api

import com.google.gson.annotations.SerializedName

data class OrionVersion(
    @SerializedName("orion") val orion: Orion
)