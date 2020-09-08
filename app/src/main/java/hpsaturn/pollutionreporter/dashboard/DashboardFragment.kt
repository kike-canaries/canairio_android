package hpsaturn.pollutionreporter.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.dashboard.data.services.AqicnApiFeedService
import hpsaturn.pollutionreporter.dashboard.presentation.DashboardViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val TAG = DashboardFragment::class.java.simpleName

    private val dashboardViewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var aqicnApiFeedService: AqicnApiFeedService

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsGranted ->
            if (!permissionsGranted.containsValue(false)) {
                Toast.makeText(requireContext(), "Thanks!", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        displayAirQualityIndexOnView()
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    private fun displayAirQualityIndexOnView() {
        fetchLastLocation()?.addOnSuccessListener {
            dashboardViewModel.airQualityStatusLiveData.observe(viewLifecycleOwner, Observer {
                Log.d(TAG, "onCreateView: $it")
                airQualittyIdexText.text = "${it.airQualityIndex}"
            })
        }
    }

    private fun fetchLastLocation(): Task<Location>? {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            return null
        }
        return fusedLocationProviderClient.lastLocation
    }

}
