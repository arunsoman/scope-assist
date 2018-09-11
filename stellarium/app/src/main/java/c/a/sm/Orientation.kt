package c.a.sm


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.arunsoman.stellarium.R
import java.math.RoundingMode
import java.text.DecimalFormat

class Orientation: Fragment(), OrientationDto.Listener {


    private lateinit var mAzimuthCurrent: TextView
    private lateinit var mPitchCurrent: TextView
    private lateinit var mRollCurrent: TextView
    private lateinit var o: OrientationDto

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.orientation, container, false)
        mAzimuthCurrent = view.findViewById(R.id.azCurrentV);
        mPitchCurrent = view.findViewById(R.id.pitchCurrentV);
        mRollCurrent = view.findViewById(R.id.rollCurrentV);
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        o = OrientationDto()
        o.start(activity!!, this)

    }


    override fun onOrientationChanged(az: Float, pitch: Float, roll: Float) {
        mAzimuthCurrent.setText(az.fromRadToHourMinSec())
        mPitchCurrent.setText(pitch.fromRadToHourMinSec())
        mRollCurrent.setText(roll.fromRadToHourMinSec())

    }


    override fun onStop() {
        super.onStop()
        o.stopListening()
    }

    override fun onPause() {
        super.onPause()
        o.stopListening()
    }

    override fun onResume() {
        super.onResume()
        o.start(activity!!, this)
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


}