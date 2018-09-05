package c.a.sm

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import c.a.sm.nio.StellariumClient
import com.example.arunsoman.stellarium.R

class SkyObject:Fragment() {
    var objName: String  =""
    private lateinit var client: StellariumClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.sky_object_location, container, false)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        client = StellariumClient()
        client.init(context!!)
        view.findViewById<EditText>(R.id.objectName).setText(objName)
        view.findViewById<Button>(R.id.getDetails).setOnClickListener{
            client.sendRequest(
                    "http://192.168.17.173:8090/api/objects/info?name=${view.findViewById<EditText>(R.id.objectName).getText()}",
                    onSuccess = {s:String->
                        activity!!.runOnUiThread(Runnable {
                            if(s.toLowerCase().equals("object name not found")){
                                view.findViewById<TextView>(R.id.radec).setText("object name not found")
                                view.findViewById<TextView>(R.id.altaz).setText("object name not found")
                            }
                            else {
                                val radec = s.split(">RA/Dec (J2000.0)")[1].split("<br")[0].trim()
                                view.findViewById<TextView>(R.id.radec).setText("Ra/Dec:${radec}")
                                val altaz = s.split("Az./Alt")[1].split("<br")[0].trim()
                                view.findViewById<TextView>(R.id.altaz).setText("Az./Alt:${altaz}")
                            }
                            })
                    },
                    onFailure = {s:String->
                        activity!!.runOnUiThread(Runnable {

                            view.findViewById<TextView>(R.id.radec).setText(s)

                            view.findViewById<TextView>(R.id.altaz).setText(s)})
                    }
                    )
        }
    }
}