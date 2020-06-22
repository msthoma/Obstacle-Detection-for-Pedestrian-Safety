package cy.org.rise.obsai.utils

import java.util.*

/**
 * Returns activity name for logging purposes, see discussions
 * [1](https://stackoverflow.com/a/52956934) and  [2](https://stackoverflow.com/a/57123619).
 */
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/**
 * Rounds double to specified decimal points, see [discussion](https://stackoverflow.com/a/59513133).
 *
 * @param n Number of decimal points to round to
 * @return Rounded double
 */
fun Double.roundTo(n: Int = 3): Double = "%.${n}f".format(Locale.ENGLISH, this).toDouble()

/**
 * Rounds float to specified decimal points, see [discussion](https://stackoverflow.com/a/59513133).
 *
 * @param n Number of decimal points to round to
 * @return Rounded float
 */
fun Float.roundTo(n: Int = 3): Float = "%.${n}f".format(Locale.ENGLISH, this).toFloat()