package cy.org.rise.obsai.data

data class OrientationReadings(
    val accelerometer: FloatArray,
    val compass: FloatArray,
    val orientation: FloatArray
)
