// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.abbyy.mobile.uicomponents.scenario.MultiPageImageCaptureScenario
import com.abbyy.rtr.ui.sample.datacapture.capture.SharedEngine
import com.abbyy.rtr.ui.sample.datacapture.data.ProfileResultStorage
import kotlinx.android.synthetic.main.fragment_capture_image.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageCaptureFragment : Fragment(R.layout.fragment_capture_image), MultiPageImageCaptureScenario.Callback {
    companion object {
        private const val LOG_TAG = "ImageCaptureFragment"

        fun newInstance() = ImageCaptureFragment()
    }

    private lateinit var storage: ProfileResultStorage
    private lateinit var systemBarsAppearance: SystemBarsAppearance

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = App.profileResultStorage
        systemBarsAppearance = SystemBarsAppearance(window = requireActivity().window)

        val scenario = MultiPageImageCaptureScenario
            .Builder(SharedEngine.get(), requireContext())
            .setRequiredPageCount(1)
            .setShowPreviewEnabled(true)
            .build()
        scenario.setCallback(this)
        captureView.setCaptureScenario(scenario)
    }

    override fun onStart() {
        super.onStart()
        systemBarsAppearance.apply()
    }

    override fun onResume() {
        super.onResume()
        captureView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        captureView.stopCamera()
    }

    override fun onStop() {
        super.onStop()
        systemBarsAppearance.restore()
    }

    override fun onClose(result: MultiPageImageCaptureScenario.Result) {
        clearDataAndClose(result)
    }

    override fun onError(exception: Exception, result: MultiPageImageCaptureScenario.Result) {
        Log.d(LOG_TAG, "capture error", exception)
        clearDataAndClose(result)
    }

    override fun onFinished(result: MultiPageImageCaptureScenario.Result) {
        lifecycleScope.launchWhenStarted {
            val image = withContext(Dispatchers.Default) {
                val pageId = result.getPages().first()
                result.loadImage(pageId)
            }
            storage.updateImage(image)
            clearDataAndClose(result)
        }
    }

    private fun clearDataAndClose(result: MultiPageImageCaptureScenario.Result) {
        lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.Default) {
                result.clear()
            }
            close()
        }
    }

    private fun close() {
        (requireActivity() as AppActivity).closeRetakeImageScreen()
    }
}
