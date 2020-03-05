package cy.org.rise.obsai.utils

/**
 * Returns activity name for logging purposes
 * see https://stackoverflow.com/a/52956934/3755276
 * also https://stackoverflow.com/a/57123619/3755276
 */
inline fun <reified T> T.TAG(): String = T::class.java.simpleName