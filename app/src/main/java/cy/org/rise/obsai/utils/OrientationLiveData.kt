package cy.org.rise.obsai.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData

class OrientationLiveData(context: Context) : LiveData<OrientationReadings>(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onInactive() {
        super.onInactive()
        Log.d(TAG(), "orientation tracking ended")
        sensorManager.unregisterListener(this)
    }

    override fun onActive() {
        super.onActive()
        Log.d(TAG(), "orientation tracking started")
        sensorManager.apply {
            getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
                registerListener(
                    this@OrientationLiveData, accelerometer, SensorManager
                        .SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI
                )
            }

            getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
                registerListener(
                    this@OrientationLiveData, magneticField, SensorManager
                        .SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG(), "Sensor accuracy changed to $accuracy")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> System.arraycopy(
                    event.values, 0,
                    accelerometerReading, 0, accelerometerReading.size
                )

                Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(
                    event.values, 0,
                    magnetometerReading, 0, magnetometerReading.size
                )
            }

            // Calculate device orientation by combining accelerometer and magneticField, see
            // https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
            // Results may need to be translated using remapCoordinateSystem(), see
            // https://developer.android.com/reference/android/hardware/SensorManager#remapCoordinateSystem(float[],%20int,%20int,%20float[])
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // update live data
            value = OrientationReadings(
                accelerometerReading,
                magnetometerReading,
                orientationAngles
            )
        }
    }
}

data class OrientationReadings(
    val accelerometer: FloatArray,
    val compass: FloatArray,
    val orientation: FloatArray
)