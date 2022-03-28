// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario
import com.abbyy.mobile.uicomponents.scenario.DataCaptureScenario.ResourceType
import com.abbyy.rtr.ui.sample.datacapture.capture.DataCaptureConfig
import com.abbyy.rtr.ui.sample.datacapture.capture.SharedEngine
import com.abbyy.rtr.ui.sample.datacapture.capture.getConfigs
import com.abbyy.rtr.ui.sample.datacapture.data.Profile
import com.abbyy.rtr.ui.sample.datacapture.data.ProfileCaptureResult
import com.abbyy.rtr.ui.sample.datacapture.data.ProfileResultStorage
import kotlinx.android.synthetic.main.fragment_capture_data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// Fragment
class DataCaptureFragment :
    Fragment(R.layout.fragment_capture_data),
    ActivityCompat.OnRequestPermissionsResultCallback,
    DataCaptureScenario.Callback,
    DataCaptureScenario.CloseListener {

    companion object {
        private const val PROFILE_KEY = "profile_key"
        private const val STATE_INDEX_KEY = "state_index_key"
        private const val LOG_TAG = "DataCaptureFragment"

        fun newInstance(profile: Profile): Fragment {
            val fragment = DataCaptureFragment()
            val arguments = Bundle()
            arguments.putSerializable(PROFILE_KEY, profile)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var storage: ProfileResultStorage
    private lateinit var scenario: DataCaptureScenario

    private lateinit var profile: Profile
    private lateinit var captureConfigs: List<DataCaptureConfig>
    private var stateIndex: Int = 0

    private lateinit var systemBarsAppearance: SystemBarsAppearance

    private var errorDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        initializeState(savedInstanceState)
        lifecycleScope.launchWhenStarted { setupCapture() }
    }

    private fun setupDependencies() {
        storage = App.profileResultStorage
        systemBarsAppearance = SystemBarsAppearance(window = requireActivity().window)
    }

    private fun initializeState(savedInstanceState: Bundle?) {
        profile = checkNotNull(arguments?.getSerializable(PROFILE_KEY) as? Profile)
        stateIndex = savedInstanceState?.getInt(STATE_INDEX_KEY) ?: 0
        captureConfigs = getConfigs(profile)
    }

    private suspend fun setupCapture() {
        scenario = DataCaptureScenario(SharedEngine.get())
        configureScenario(scenario, captureConfigs[stateIndex])
        captureView.setCaptureScenario(scenario)
        scenario.setCloseListener(this)

        // This is not required, but it is useful to control visibility of your additional views.
        // Check onRequestPermissionsResult method for details.
        captureView.getExtendedSettings().setRequestPermissionsResultCallback(this)
        // CaptureView handles camera permission automatically.
        // You can delegate your permissions to CaptureView as following:
        // captureView.getExtendedSettings().setAdditionalPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private suspend fun configureScenario(scenario: DataCaptureScenario, config: DataCaptureConfig) {
        val settings = scenario.dataCaptureSettings
        when {
            config.profileName != null -> settings.setProfile(config.profileName)
            config.customProfileScheme != null -> {
                settings.setProfile("Custom")
                settings.setScheme(config.customProfileScheme)
            }
            else -> throw IllegalStateException("Undefined profile")
        }
        if (config.areaOfInterest != null) {
            settings.setAreaOfInterest(config.areaOfInterest)
        }
        if (config.customProfileScheme != null) {
            settings.setSelectableRecognitionLanguages()
        } else {
            val availableLanguages = settings.getAvailableRecognitionLanguages()
            val languages = storage.loadLanguages(profile = profile)
                .filter { language -> language in availableLanguages }
                .toTypedArray()
            settings.setRecognitionLanguages(*languages)
        }
        try {
            settings.checkAndApply()
        } catch (e: Exception) {
            closeScreen()
        }
        scenario.setUISettings(object : DataCaptureScenario.UISettings {
            override fun getString(type: ResourceType): String? {
                val stringRes = config.resourceTypeToStringRes[type] ?: return null
                return resources.getString(stringRes)
            }
        })
        if (config.shouldUserClickToStart) {
            scenario.stop()
        } else {
            scenario.start()
        }
        captureView.getUISettings().setGalleryButtonVisible(config.isGalleryCaptureVisible)
        scenario.setCloseListener(this)
    }

    private fun closeScreen() {
        requireFragmentManager().popBackStack()
    }

    override fun onStart() {
        super.onStart()
        systemBarsAppearance.apply()
    }

    override fun onResume() {
        super.onResume()
        scenario.setCallback(this)
        captureView.startCamera()
    }

    override fun onPause() {
        captureView.stopCamera()
        scenario.setCallback(null)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        systemBarsAppearance.restore()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX_KEY, stateIndex)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // CaptureView result is the only permission flow in this fragment,
        // so we don't check requestCode.
        val isAllPermissionsGranted =
            grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }
        captureView.getUISettings().setCancelButtonVisible(isAllPermissionsGranted)
    }

    override fun onDataCaptured(result: DataCaptureScenario.Result) {
        lifecycleScope.launchWhenCreated {
            if (result.fields.isEmpty()) {
                onError(Exception("No data detected. Recognition result empty"))
            } else {
                showProgress()
                saveResult(result)
                scheduleNextState()
                hideProgress()
            }
        }
    }

    private fun showProgress() {
        scenario.setGalleryButtonEnabled(false)
    }

    private fun hideProgress() {
        scenario.setGalleryButtonEnabled(true)
    }

    private suspend fun saveResult(result: DataCaptureScenario.Result) {
        val fields = captureConfigs[stateIndex].fieldsConverter(result.fields)
        if (stateIndex == 0) {
            storage.store(
                result = ProfileCaptureResult(
                    profile = profile,
                    image = result.image.croppedBitmap,
                    fields = fields
                )
            )
        } else {
            val storedResult = checkNotNull(storage.load())
            storage.store(storedResult.copy(fields = storedResult.fields + fields))
        }
    }

    private suspend fun scheduleNextState() {
        stateIndex += 1
        check(stateIndex <= captureConfigs.size)
        if (stateIndex == captureConfigs.size) {
            closeScreen()
            (requireActivity() as AppActivity).openProfileDetailScreen()
        } else {
            delay(2000)
            configureScenario(scenario, captureConfigs[stateIndex])
        }
    }

    override fun onError(exception: Exception) {
        if (context == null) {
            return
        }
        Log.e(LOG_TAG, "capture error", exception)
        errorDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.capture_error)
            .setMessage(exception.message)
            .setOnDismissListener { scenario.start() }
            .create()
        errorDialog?.show()
    }

    override fun onClose() = closeScreen()

    override fun onDestroyView() {
        runBlocking {
            storage.storeLanguages(
                profile = profile,
                languages = scenario.dataCaptureSettings.getRecognitionLanguages()
            )
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        errorDialog?.dismiss()
        errorDialog = null
    }
}
