package c.a.sm

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.example.arunsoman.stellarium.R
import java.math.RoundingMode
import java.text.DecimalFormat


class Level:Fragment(), SensorEventListener  {
    private val VALUE_DRIFT = 0.05f

    // System sensor manager instance.
    private lateinit var mSensorManager: SensorManager

    // Accelerometer and magnetometer sensors, as retrieved from the
    // sensor manager.
    private lateinit var mSensorAccelerometer: Sensor
    private lateinit var mSensorMagnetometer: Sensor

    // Current data from accelerometer & magnetometer.  The arrays hold values
    // for X, Y, and Z.
    private  var mAccelerometerData = FloatArray(3)
    private  var mMagnetometerData = FloatArray(3)

    // TextViews to display current sensor values.
    private lateinit var mTextSensorAzimuth: TextView
    private lateinit var mTextSensorPitch: TextView
    private lateinit var mTextSensorRoll: TextView

    // ImageView drawables to display spots.
    private lateinit var mSpotTop: ImageView
    private lateinit var mSpotBottom: ImageView
    private lateinit var mSpotLeft: ImageView
    private lateinit var mSpotRight: ImageView

    // System display. Need this for determining rotation.
    private lateinit var mDisplay: Display

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.level, container, false)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTextSensorAzimuth = view.findViewById(R.id.value_azimuth);
        mTextSensorPitch = view.findViewById(R.id.value_pitch);
        mTextSensorRoll = view.findViewById(R.id.value_roll);
        mSpotTop = view.findViewById(R.id.spot_top);
        mSpotBottom = view.findViewById(R.id.spot_bottom);
        mSpotLeft = view.findViewById(R.id.spot_left);
        mSpotRight = view.findViewById(R.id.spot_right);

        mSensorManager = activity!!.getSystemService(
                Context.SENSOR_SERVICE) as SensorManager
        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER)
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD)

        // Get the display from the window manager (for rotation).
        var wm = activity!!.getSystemService(WINDOW_SERVICE) as WindowManager
        mDisplay = wm.defaultDisplay

        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    fun Double.abs():Double{
        return Math.abs(this)
    }
    fun Int.abs():Int  {return Math.abs(this)}

    fun Float.fromRadToHourMinSec():String{
        val degress = this*180/Math.PI
        val hours = degress.toInt()
        var remains = (degress.abs() - hours.abs())*60
        val mins = remains.toInt()
        remains = remains - mins
        val sec = remains*60
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val out = "${hours} : ${mins} : ${df.format(sec.abs())}"
        return out
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        // The sensor type (as defined in the Sensor class).
        val sensorType = sensorEvent.sensor.type

        // The sensorEvent object is reused across calls to onSensorChanged().
        // clone() gets a copy so the data doesn't change out from under us
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> mAccelerometerData = sensorEvent.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> mMagnetometerData = sensorEvent.values.clone()
            else -> return
        }
        // Compute the rotation matrix: merges and translates the data
        // from the accelerometer and magnetometer, in the device coordinate
        // system, into a matrix in the world's coordinate system.
        //
        // The second argument is an inclination matrix, which isn't
        // used in this example.
        val rotationMatrix = FloatArray(9)
        val rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData)

        // Remap the matrix based on current device/activity rotation.
        var rotationMatrixAdjusted = FloatArray(9)
        when (mDisplay.rotation) {
            Surface.ROTATION_0 -> rotationMatrixAdjusted = rotationMatrix.clone()
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                    rotationMatrixAdjusted)
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                    rotationMatrixAdjusted)
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                    rotationMatrixAdjusted)
        }

        // Get the orientation of the device (azimuth, pitch, roll) based
        // on the rotation matrix. Output units are radians.
        val orientationValues = FloatArray(3)
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrixAdjusted,
                    orientationValues)
        }

        // Pull out the individual values from the array.
        val azimuth = orientationValues[0]
        var pitch = orientationValues[1]
        var roll = orientationValues[2]

        // Pitch and roll values that are close to but not 0 cause the
        // animation to flash a lot. Adjust pitch and roll to 0 for very
        // small values (as defined by VALUE_DRIFT).
        if (Math.abs(pitch) < VALUE_DRIFT) {
            pitch = 0f
        }
        if (Math.abs(roll) < VALUE_DRIFT) {
            roll = 0f
        }

        // Fill in the string placeholders and set the textview text.
        mTextSensorAzimuth.setText(azimuth.fromRadToHourMinSec())
        mTextSensorPitch.setText(pitch.fromRadToHourMinSec())
        mTextSensorRoll.setText(roll.fromRadToHourMinSec())

        // Reset all spot values to 0. Without this animation artifacts can
        // happen with fast tilts.
        mSpotTop.alpha = 0f
        mSpotBottom.alpha = 0f
        mSpotLeft.alpha = 0f
        mSpotRight.alpha = 0f

        // Set spot color (alpha/opacity) equal to pitch/roll.
        // this is not a precise grade (pitch/roll can be greater than 1)
        // but it's close enough for the animation effect.
        if (pitch > 0) {
            mSpotBottom.alpha = pitch
        } else {
            mSpotTop.alpha = Math.abs(pitch)
        }
        if (roll > 0) {
            mSpotLeft.alpha = roll
        } else {
            mSpotRight.alpha = Math.abs(roll)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mSensorManager.unregisterListener(this);
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
}