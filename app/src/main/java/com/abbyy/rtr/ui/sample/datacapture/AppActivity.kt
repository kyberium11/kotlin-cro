// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.abbyy.rtr.ui.sample.datacapture.capture.EngineTroubleshootingDialog
import com.abbyy.rtr.ui.sample.datacapture.capture.SharedEngine
import com.abbyy.rtr.ui.sample.datacapture.data.Profile

class AppActivity : AppCompatActivity() {
    companion object {
        private const val PROFILE_DETAIL_BACK_STACK = "profile_detail_back_stack"
        private const val RETAKE_IMAGE_BACK_STACK = "retake_image"
        private const val PROFILE_DETAIL_TAG = "profile_detail_tag"
    }

    private val contentFrameLayout = R.id.contentFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Call it before an if block because the process can be killed while the app is in the recent apps list.
        // Then savedInstanceState will be not null, but the engine won't be initialized.

        // Call it before an if block because the process can be killed while the app is in the recent apps list.
        // Then savedInstanceState will be not null, but the engine won't be initialized.
        val isInitialized = initializeEngine()
        if (savedInstanceState == null) {
            if (isInitialized) {
                openProfileListScreen()
            }
        }
    }

    private fun initializeEngine(): Boolean {
        return try {
            SharedEngine.initialize(application)
            true
        } catch (throwable: Throwable) {
            val dialogFragment: DialogFragment = EngineTroubleshootingDialog.create(throwable)
            dialogFragment.show(supportFragmentManager, null)
            false
        }
    }

    private fun openProfileListScreen() {
        supportFragmentManager
            .beginTransaction()
            .replace(contentFrameLayout, ProfileListFragment.newInstance())
            .commit()
    }

    fun openProfileDetailScreen() {
        supportFragmentManager.popBackStack(
            PROFILE_DETAIL_BACK_STACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager
            .beginTransaction()
            .replace(
                contentFrameLayout,
                ProfileDetailFragment.newInstance(),
                PROFILE_DETAIL_TAG
            )
            .addToBackStack(PROFILE_DETAIL_BACK_STACK)
            .commit()
    }

    fun openDataCaptureScreen(profile: Profile) {
        supportFragmentManager
            .beginTransaction()
            .replace(contentFrameLayout, DataCaptureFragment.newInstance(profile))
            .addToBackStack(null)
            .commit()
    }

    fun openRetakeImageScreen() {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(contentFrameLayout, ImageCaptureFragment.newInstance())
            .addToBackStack(RETAKE_IMAGE_BACK_STACK)

        transaction.commit()
    }

    fun closeRetakeImageScreen() {
        supportFragmentManager.popBackStack(
            RETAKE_IMAGE_BACK_STACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    fun dismissKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

}
