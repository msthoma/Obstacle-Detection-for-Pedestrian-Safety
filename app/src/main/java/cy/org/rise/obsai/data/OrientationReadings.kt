package cy.org.rise.obsai.data

/**
 * Data class that combines measurements from accelerometer and compass, as well as calculated
 * orientation angles based on the the former two.
 *
 * See [this](https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient)
 * for an explanation on how the orientation angles are calculated.
 *
 * @property accelerometer readings from the accelerometer
 * @property compass readings from the compass
 * @property orientation calculated orientation angles
 */
data class OrientationReadings(
    val accelerometer: FloatArray,
    val compass: FloatArray,
    val orientation: FloatArray
)
