package com.rippleeffect.fleettracking.mvvm.control

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.rippleeffect.fleettracking.R
import com.rippleeffect.fleettracking.databinding.FragmentControlBinding
import com.rippleeffect.fleettracking.mvvm.base.BaseView
import com.rippleeffect.fleettracking.service.LocationService
import com.rippleeffect.fleettracking.util.BatteryUtils
import com.rippleeffect.fleettracking.util.LocationUtils
import com.rippleeffect.fleettracking.util.ServicesUtils
import dagger.hilt.android.AndroidEntryPoint
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback
import timber.log.Timber


@AndroidEntryPoint
class ControlFragment : Fragment(), BaseView<ControlState.ViewState, ControlState.ViewAction> {


    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!


    companion object {
        var isServiceStoppedFromUser = false
        fun newInstance() = ControlFragment()
    }

    private val viewModel: ControlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Nammu.init(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToViewModel()

        binding.btnStart.setOnClickListener {
            checkPermissionsAndStartService()
        }

        binding.btnStop.setOnClickListener {
            stopForegroundService()
        }

        binding.btnDisableAllArmTrigger.setOnClickListener {
            viewModel.setAlarmEnabled(false)
            refreshServiceButtons()
        }
        binding.btnEnableAlarmTrigger.setOnClickListener {
            viewModel.setAlarmEnabled(true)
            refreshServiceButtons()
        }

        binding.btnEnableFiltering.setOnClickListener {
            viewModel.setFilteringEnabled(true)
            refreshServiceButtons()
        }
        binding.btnDisableFiltering.setOnClickListener {
            viewModel.setFilteringEnabled(false)
            refreshServiceButtons()
        }


        refreshServiceButtons()


    }

    override fun onResume() {
        super.onResume()
        //Now they are granted but denied earlier from phone settings
        if (arePermissionsGranted() &&
            viewModel.arePermissionsDisabled() && !BatteryUtils.isBatterySaverEnabled(requireContext())
        ) {
            restartService()
        }
    }


    private fun arePermissionsGranted(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && Nammu.checkPermission(
                Manifest.permission.ACTIVITY_RECOGNITION
            ) && Nammu.checkPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) && !BatteryUtils.isBatterySaverEnabled(requireContext())
        } else {
            Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && !BatteryUtils.isBatterySaverEnabled(
                requireContext()
            )
        }
    }

    private fun checkPermissionsAndStartService() {
        if (ServicesUtils.isMyServiceRunning(LocationService::class.java, requireActivity())) return
        if (LocationUtils.isLocationEnabled(requireContext())) {
            checkLocationPermission()
            return
        }

        //Location not enabled
        showLocationDisabledDialog()


    }

    private fun showLocationDisabledDialog() {
        val dialog = MaterialDialog(requireContext())
            .title(text = getString(R.string.location_disabled))
            .message(text = getString(R.string.enable_location))
            .positiveButton(text = getString(R.string.open_location_setting)) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .negativeButton(text = getString(R.string.cancel)) {
            }


        dialog.cancelable(false)
        dialog.show()

    }


    override fun processState(state: ControlState.ViewState) {

        when (state) {
            ControlState.ViewState.LoadingError -> {
            }

        }


    }


    override fun processAction(action: ControlState.ViewAction) {
        when (action) {
            ControlState.ViewAction.CloseApp -> TODO()
        }
    }

    override fun subscribeToViewModel() {
        viewModel.viewState.observe(requireActivity(), ::processState)
        viewModel.viewAction.observe(requireActivity(), ::processAction)
    }


    private fun checkLocationPermission() {
        val isPermissionGranted = Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (isPermissionGranted) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                checkActivityRecognitionPermission()
            } else {
                startForegroundService()
            }
            return
        }

        Nammu.askForPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionCallback {
                override fun permissionGranted() {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        checkActivityRecognitionPermission()
                    } else {
                        startForegroundService()
                    }
                }

                override fun permissionRefused() {
                    showMessage(getString(R.string.location_permission))
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkActivityRecognitionPermission() {
        val isPermissionGranted = Nammu.checkPermission(Manifest.permission.ACTIVITY_RECOGNITION)
        if (isPermissionGranted) {
            checkBackgroundLocationPermission()
            return
        }

        Nammu.askForPermission(this,
            Manifest.permission.ACTIVITY_RECOGNITION, object : PermissionCallback {
                override fun permissionGranted() {
                    checkBackgroundLocationPermission()
                }

                override fun permissionRefused() {
                    showMessage(getString(R.string.activity_recognition_permission))
                }
            })
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkBackgroundLocationPermission() {
        val isPermissionGranted = Nammu.checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (isPermissionGranted) {
            startForegroundService()
            return
        }

        Nammu.askForPermission(this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION, object : PermissionCallback {
                override fun permissionGranted() {
                    startForegroundService()
                }

                override fun permissionRefused() {
                    showMessage(getString(R.string.background_location_permission))
                }
            })
    }


    private fun startForegroundService() {
        isServiceStoppedFromUser = false

        requireContext().startService(
            LocationService.getLocationServiceIntent(
                requireContext(),
                LocationService.ACTION_START
            )
        )
        refreshServiceButtons()
    }

    private fun stopForegroundService() {
        val stopServiceIntent = Intent(requireContext(), LocationService::class.java)
        isServiceStoppedFromUser = true
        requireActivity().stopService(stopServiceIntent)
        refreshServiceButtons()
    }

    private fun restartService() {
        val stopServiceIntent = Intent(requireContext(), LocationService::class.java)
        requireActivity().stopService(stopServiceIntent)

    }


    /**
     * Shows appropriate button
     */
    private fun refreshServiceButtons() {
        if (ServicesUtils.isMyServiceRunning(LocationService::class.java, requireActivity())) {
            binding.btnStart.visibility = View.GONE
            binding.btnStop.visibility = View.VISIBLE
        } else {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStop.visibility = View.GONE
        }

        if (viewModel.isAlarmEnabled()) {
            binding.btnEnableAlarmTrigger.visibility = View.GONE
            binding.btnDisableAllArmTrigger.visibility = View.VISIBLE
        } else {
            binding.btnEnableAlarmTrigger.visibility = View.VISIBLE
            binding.btnDisableAllArmTrigger.visibility = View.GONE
        }


        if (viewModel.isFilteringEnabled()) {
            binding.btnEnableFiltering.visibility = View.GONE
            binding.btnDisableFiltering.visibility = View.VISIBLE
        } else {
            binding.btnEnableFiltering.visibility = View.VISIBLE
            binding.btnDisableFiltering.visibility = View.GONE
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
            .show()
    }

    private fun testMethod() {
        val POWERMANAGER_INTENTS = arrayOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.htc.pitroad",
                    "com.htc.pitroad.landingpage.activity.LandingPageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.MainActivity"
                )
            )
        )



        for (intent in POWERMANAGER_INTENTS) if (requireActivity().getPackageManager()
                .resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                ) != null
        ) {
            Timber.d("NASLOGAJE   aasdfasdfsfdasdfafasdfas")
            // show dialog to ask user action
            break
        }
    }

}