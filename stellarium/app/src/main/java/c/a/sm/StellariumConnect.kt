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


class StellariumConnect : Fragment() {
    private lateinit var client: StellariumClient
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.stellarium_connect, container, false)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        client = StellariumClient()
        client.init(context!!)
        view.findViewById<Button>(R.id.connect_btn).setOnClickListener {
            client.sendRequest(view.findViewById<EditText>(R.id.stellarium_uri).text.toString()+"/api/main/status",
                    {s:String->
                        activity!!.runOnUiThread(Runnable {
                            Log.d("text", s)
                            view.findViewById<TextView>(R.id.result).setText(s)})},
                    {s:String->
                        activity!!.runOnUiThread(Runnable {
                            view.findViewById<TextView>(R.id.result).setText(s)})})
        }
    }

}
