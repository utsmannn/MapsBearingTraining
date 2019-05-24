package com.utsman.mapsbearing

import com.google.android.gms.location.LocationRequest

val locationRequest: LocationRequest = LocationRequest.create()
    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

fun locationRequestWithInterval(interval: Long): LocationRequest = LocationRequest.create()
    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    .setInterval(interval)