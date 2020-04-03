package cy.org.rise.obsai.api

data class OrionVersion(
    val orion: Orion
) {
    data class Orion(
        val version: String,
        val uptime: String,
        val git_hash: String,
        val compile_time: String,
        val compiled_by: String,
        val compiled_in: String,
        val release_date: String,
        val doc: String
    )
}