package cy.org.rise.obsai.api

import com.google.gson.annotations.SerializedName

data class RestObstacle(
    @SerializedName("id") val id: String,

    @SerializedName("obstacleType") var obstacleType: ObstacleType,

    @SerializedName("Location") var location: Location,

    @SerializedName("type") val type: String = "Obstacle"
) {
    data class ObstacleType(
        @SerializedName("value") val value: String,
        @SerializedName("type") val type: String = "Text"
    )

    data class Location(
        @SerializedName("value")
        val value: Value,
        @SerializedName("type")
        val type: String = "geo:json"
    ) {
        data class Value(
            @SerializedName("coordinates")
            val coordinates: List<Double>,
            @SerializedName("type")
            val type: String = "Point"
        )
    }
}
