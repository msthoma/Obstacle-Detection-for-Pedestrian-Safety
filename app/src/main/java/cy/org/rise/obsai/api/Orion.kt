package cy.org.rise.obsai.api

import com.google.gson.annotations.SerializedName

/**
 * Made using https://www.json2kotlin.com/
 */

data class Orion(
    @SerializedName("version") val version: String,
    @SerializedName("uptime") val uptime: String,
    @SerializedName("git_hash") val git_hash: String,
    @SerializedName("compile_time") val compile_time: String,
    @SerializedName("compiled_by") val compiled_by: String,
    @SerializedName("compiled_in") val compiled_in: String,
    @SerializedName("release_date") val release_date: String,
    @SerializedName("doc") val doc: String
)