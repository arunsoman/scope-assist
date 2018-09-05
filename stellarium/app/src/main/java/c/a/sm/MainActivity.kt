package c.a.sm

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.example.arunsoman.stellarium.R
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pageAdapter = PageAdapter(supportFragmentManager)

        val viewPager = findViewById(R.id.viewpager) as ViewPager
        viewPager.adapter = pageAdapter
        val tabLayout = findViewById(R.id.tablayout) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        retrievePermissions().forEach { perm:String->

        if (ContextCompat.checkSelfPermission(this, perm)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,perm)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(perm),
                        99)


            }
        } else {
            // Permission has already been granted
        }
        }
    }

    fun retrievePermissions(): Array<String> {
        try {
            return this
                    .getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException("This should have never happened.", e)
        }

    }
}

class PageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    class Holder(var n:String, var f:Fragment)
    val map = HashMap<Int, Holder>()
    var cnt:Int = 0


    init {
        val polaris = SkyObject()
        polaris.objName="polaris"
       map.put(cnt++, Holder("Level", Level()))
       map.put(cnt++, Holder("Stalleraium connect", StellariumConnect()))
       map.put(cnt++, Holder("GPS ", Location()))
       map.put(cnt++, Holder("Polaris ", polaris))
       map.put(cnt++, Holder("Target ", SkyObject()))
    }


    override fun getCount(): Int {
        return map.size
    }

    override fun getItem(position: Int): Fragment {
        return map[position]!!.f
    }

    override fun getPageTitle(position: Int): CharSequence {
        return map[position]!!.n
    }
}
