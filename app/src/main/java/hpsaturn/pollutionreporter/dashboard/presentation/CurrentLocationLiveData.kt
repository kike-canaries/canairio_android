package hpsaturn.pollutionreporter.dashboard.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.qualifiers.ApplicationContext
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.core.domain.entities.ErrorResult
import hpsaturn.pollutionreporter.core.domain.entities.InProgress
import hpsaturn.pollutionreporter.core.domain.entities.Result
import hpsaturn.pollutionreporter.core.domain.entities.Success
import hpsaturn.pollutionreporter.core.domain.errors.PermissionException
import hpsaturn.pollutionreporter.core.domain.errors.PermissionNotGrantedException
import javax.inject.Inject

class CurrentLocationLiveData @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest,
    private val dexter: DexterBuilder.Permission,
    @ApplicationContext private val context: Context
) : LiveData<Result<@JvmSuppressWildcards Location>>() {

    private val setLocationListener = { location: Location -> value = Success(location) }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        postValue(InProgress)
        checkPermissions { startLocationUpdates() }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it == null) {
                postValue(ErrorResult(PermissionNotGrantedException(context.getString(R.string.check_location_settings))))
            } else {
                it.also { setLocationListener(it) }
            }
        }
    }

    private fun checkPermissions(onPermissionsGranted: () -> Unit) {
        dexter.withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport?) {
                if (multiplePermissionsReport?.areAllPermissionsGranted() == true) {
                    onPermissionsGranted()
                } else {
                    postValue(ErrorResult(PermissionNotGrantedException(context.getString(R.string.location_permissions_not_granted_error))))
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                requests: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                postValue(ErrorResult(PermissionException(context.getString(R.string.enable_permission_request))))
                token?.continuePermissionRequest()
            }

        }).withErrorListener { postValue(ErrorResult(IllegalAccessException(it.name))) }.check()
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                setLocationListener(location)
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (areLocationPermissionsGranted()) {
            postValue(ErrorResult(PermissionNotGrantedException(context.getString(R.string.location_permissions_not_granted_error))))
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

}