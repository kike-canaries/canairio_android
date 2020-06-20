package hpsaturn.pollutionreporter.dashboard

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.hpsaturn.tools.Logger
import dagger.hilt.android.AndroidEntryPoint
import hpsaturn.pollutionreporter.BaseActivity
import hpsaturn.pollutionreporter.Config
import hpsaturn.pollutionreporter.R
import hpsaturn.pollutionreporter.service.RecordTrackScheduler
import hpsaturn.pollutionreporter.service.RecordTrackService
import kotlinx.android.synthetic.main.activity_dashboard.*

private val TAG = DashboardActivity::class.java.simpleName

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupNavigationComponent()
        startRecordTrackService()
        checkBluetoothSupport()
    }

    /**
     * Sets up the app to use Android Navigation Component. More information of this component can
     * be found here: https://developer.android.com/guide/navigation/navigation-getting-started
     */
    private fun setupNavigationComponent() {
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigation.setupWithNavController(navController)
    }

    private fun startRecordTrackService() {
        Log.i(TAG, "starting RecordTrackService..")
        val trackServiceIntent = Intent(this, RecordTrackService::class.java)
        startService(trackServiceIntent)
        RecordTrackScheduler.startScheduleService(this, Config.DEFAULT_INTERVAL)
    }


    // TODO (@juanpa097) - The code bellow this comment should be refactor.

    private fun checkBluetoothSupport() { // Use this check to determine whether BLE is supported on the device.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
        } else if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 0)
        } else Logger.i(BaseActivity.TAG, "[BLE] checkBluetoohtBle: ready!")
    }

}
