package com.utsman.mapsbearing

import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Location
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.patloew.rxlocation.RxLocation
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.completable.CompletableFromObservable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()
    private val rxLocation = RxLocation(application.applicationContext)
    private val myLocation: MutableLiveData<Location> = MutableLiveData()
    private val myLocationOnce: MutableLiveData<Location> = MutableLiveData()
    private val myAddress: MutableLiveData<Address> = MutableLiveData()
    val myBearing: MutableLiveData<Float> = MutableLiveData()

    private val loc: MutableLiveData<Location> = MutableLiveData()

    @SuppressLint("MissingPermission")
    private fun observableLocation() = run {
        rxLocation.location().updates(locationRequest)
    }

    @SuppressLint("MissingPermission")
    private fun observableLocationWithInterval(interval: Long) = run {
        rxLocation.location().updates(locationRequestWithInterval(interval))
    }


    @SuppressLint("MissingPermission")
    fun getMyLocation(interval: Long): LiveData<LocationWithBearing> {


        val locBear: MutableLiveData<LocationWithBearing> = MutableLiveData()

        disposable.add(
            observableLocationWithInterval(interval)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { location ->
                    myLocation.postValue(location)

                    //Log.i("BANGSAT2", "${location.latitude}")
                }
        )

        disposable.add(
            observableLocationWithInterval(interval/2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { location ->
                    //myLocation.postValue(location)

                    loc.postValue(location)

                    Log.i("BANGKE", "${myLocation.value?.latitude} =---= ${loc.value?.latitude}")

                    val bearing = bearingBetweenLocations(myLocation.value, loc.value)
                    myBearing.postValue(bearing)
                    Log.i("BANG====", bearing.toString())

                    locBear.postValue(LocationWithBearing(myLocation.value, myBearing.value))
                }
        )

        return locBear
        //return myLocation
    }

    fun getMyAddress(): LiveData<Address> {
        disposable.add(
            observableLocation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { location -> rxLocation.geocoding().fromLocation(location).toObservable() }
                .subscribe { address ->
                    myAddress.postValue(address)
                }
        )
        return myAddress
    }

    fun getMyLocationOnce(): LiveData<Location> {
        disposable.add(
            observableLocation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { location ->
                    myLocationOnce.postValue(location)
                }
        )

        return myLocationOnce
    }


    private fun bearingBetweenLocations(latLng1: Location?, latLng2: Location?): Float {

        var brng = 0.0

        if (latLng1 != null && latLng2 != null) {

            val lat1 = latLng1.latitude * Math.PI / 180
            val long1 = latLng1.longitude * Math.PI / 180
            val lat2 = latLng2.latitude * Math.PI / 180
            val long2 = latLng2.longitude * Math.PI / 180

            val dLon = long2 - long1

            val y = Math.sin(dLon) * Math.cos(lat2)
            val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                    * Math.cos(lat2) * Math.cos(dLon))

            brng = Math.atan2(y, x)

            brng = Math.toDegrees(brng)
            brng = (brng + 360) % 360
        }

        return brng.toFloat()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}

data class LocationWithBearing(val location: Location?,
                               val bearing: Float?)