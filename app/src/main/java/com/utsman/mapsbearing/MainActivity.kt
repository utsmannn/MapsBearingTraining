package com.utsman.mapsbearing

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.patloew.rxlocation.RxLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), PermissionListener {

    private lateinit var mapView: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mapView = map_view as SupportMapFragment

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
            .check()

    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        mapView.getMapAsync { map -> setupMap(map) }
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken) {
        token.continuePermissionRequest()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun setupMap(map: GoogleMap) {

        val locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        var finalBearing = 0.0f

        locationViewModel.getMyLocation(300).observe(this, Observer {locationWithBearing ->
            map.clear()

            val myLoc = locationWithBearing.location
            val myBearing = locationWithBearing.bearing

            if (myLoc?.latitude != null && myBearing != null) {

                val myLocationMarker = MarkerOptions().position(LatLng(myLoc.latitude, myLoc.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cycle_icon))

                if (myBearing > 0.0f) {
                    finalBearing = myBearing
                    myLocationMarker.rotation(finalBearing)
                }

                map.addMarker(myLocationMarker)

                Log.i("ANJAYLAAAA", "${myLoc.latitude} === $myBearing")
            }
        })

        locationViewModel.getMyLocationOnce().observe(this, Observer { location ->
            val myLatlng = LatLng(location.latitude, location.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatlng, 18f))
        })

        locationViewModel.getMyAddress().observe(this, Observer { address ->
            val myCityName = address.adminArea
            val myLocalityName = address.subLocality
            val myLocationName = try {
                address.featureName
            } catch (e: Exception) {
                "Unknown name location"
            }

            Toast.makeText(this, "$myLocationName, $myLocalityName, $myCityName", Toast.LENGTH_SHORT).show()

        })


    }
}
