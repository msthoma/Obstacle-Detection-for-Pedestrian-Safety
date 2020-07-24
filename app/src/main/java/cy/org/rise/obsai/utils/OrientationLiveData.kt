package cy.org.rise.obsai.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LiveData

class OrientationLiveData(context: Context) : LiveData<FloatArray>(), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onInactive() {
        super.onInactive()
        Log.d(TAG(), "orientation tracking ended")
    }

    override fun onActive() {
        super.onActive()
        Log.d(TAG(), "orientation tracking started")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }
}