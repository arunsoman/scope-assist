package c.a.sm

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager

import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.gyroscope.OrientationGyroscope;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.complimentary.OrientationFusedComplimentary;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.OrientationFusedKalman;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;




class OrientationDto:SensorEventListener {
    interface Listener {
        fun onOrientationChanged(az:Float, pitch: Float, roll: Float)
    }

    private lateinit var listener:Listener
    private lateinit var fusedOrientation:FloatArray
    private lateinit var acceleration:FloatArray
    private lateinit var magnetic:FloatArray
    private lateinit var rotation :FloatArray


    private lateinit var meanFilter: MeanFilter

    private lateinit var sensorManager: SensorManager

    private var orientationGyroscope: OrientationGyroscope
    private var orientationComplimentaryFusion: OrientationFusedComplimentary
    private var orientationKalmanFusion: OrientationFusedKalman


    init {
        orientationGyroscope = OrientationGyroscope()
        orientationComplimentaryFusion = OrientationFusedComplimentary()
        orientationKalmanFusion = OrientationFusedKalman()
    }

    fun start(activity: Activity, listener: Listener){
        this.listener = listener
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this, sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        // Register for sensor updates.
        sensorManager.registerListener(this, sensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);

        // Register for sensor updates.
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);
        orientationKalmanFusion.startFusion()
    }

    fun stopListening(){
        orientationKalmanFusion.stopFusion()
        sensorManager.unregisterListener(this)
    }

    private var hasAcceleration=false
    private var hasMagnetic=false

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleration = event.values.clone()
            hasAcceleration = true;
        } else  if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetic = event.values.clone()
            hasMagnetic = true;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            rotation = event.values.clone()
            if(!orientationKalmanFusion.isBaseOrientationSet()) {
                if(hasAcceleration && hasMagnetic) {
                    orientationKalmanFusion.setBaseOrientation(RotationUtil.getOrientationQuaternionFromAccelerationMagnetic(acceleration, magnetic));
                }
            } else {
                fusedOrientation = orientationKalmanFusion.calculateFusedOrientation(rotation, event.timestamp, acceleration, magnetic);
                listener.onOrientationChanged(fusedOrientation[0],fusedOrientation[1],fusedOrientation[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}

}