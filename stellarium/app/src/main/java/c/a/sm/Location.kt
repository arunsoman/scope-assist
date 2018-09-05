package c.a.sm

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.arunsoman.stellarium.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Demonstrates use of the Location API to retrieve the last known location for a device.
 */
class Location : Fragment() {

    private val TAG = "Gps fragement"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var altitude: TextView
    private lateinit var locationName: EditText
    private lateinit var currenttime: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.gps, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitudeText = view.findViewById(R.id.latitude)
        longitudeText = view.findViewById(R.id.longitude)
        altitude = view.findViewById(R.id.altitude)
        locationName = view.findViewById(R.id.locationName)
        currenttime = view.findViewById(R.id.currentTime)
        view.findViewById<Button>(R.id.getGPS).setOnClickListener{
                getLastLocation()
        }
        view.findViewById<Button>(R.id.updateLocation).setOnClickListener {
            //TODO update stellarium
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful && task.result != null) {
                        activity!!.runOnUiThread(Runnable {
                            val s = task.result
                            view!!.findViewById<TextView>(R.id.latitude).setText("lat:${s.latitude.toString()}")
                            view!!.findViewById<TextView>(R.id.longitude).setText("long:${s.longitude.toString()}")
                            view!!.findViewById<TextView>(R.id.altitude).setText("alt:${s.altitude.toString()}")
                            view!!.findViewById<TextView>(R.id.currentTime).setText("time:" +
                                    "${SimpleDateFormat("yyyy.MM.dd HH:mm").format(s.time) }")
                            view!!.findViewById<TextView>(R.id.accuracy).setText("accuracy:${s.accuracy.toString()}")
                        })
                        //TODO update the view
                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.exception)
                        //TODO update the view
                    }
                }
    }



}