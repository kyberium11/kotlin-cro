// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.
package com.abbyy.rtr.ui.sample.datacapture.capture

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.abbyy.mobile.rtr.Engine.LicenseException
import com.abbyy.rtr.ui.sample.datacapture.R
import java.io.IOException

object EngineTroubleshootingDialog {
    /**
     * Creates an error dialog for a throwable from [SharedEngine.initialize].
     * The dialog is used for troubleshooting.
     * Do not use in production builds.
     *
     * @see SharedEngine
     */
    fun create(throwable: Throwable): DialogFragment {
        return when (throwable) {
            is IOException -> {
                createDialogWithMessage(
                    "Could not load some required resource files. Make sure to configure " +
                            "'assets' directory in your application and specify correct 'license file name'. " +
                            "See logcat for details."
                )
            }
            is LicenseException -> {
                createDialogWithMessage(
                    "License not valid. Make sure you have a valid license file in the " +
                            "'assets' directory and specify correct 'license file name' and 'application id'. " +
                            "See logcat for details."
                )
            }
            else -> {
                createDialogWithMessage(
                    "Unspecified error while loading the engine. See logcat for details."
                )
            }
        }
    }

    private fun createDialogWithMessage(message: String): DialogFragment {
        return TroubleshootingDialog.newInstance(message)
    }

    class TroubleshootingDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.abbyy_mi_sdk)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok) { _, _ -> finishActivity() }
                .create()
        }

        private fun finishActivity() {
            requireActivity().finish()
        }

        private val message: String?
            get() {
                val arguments = requireArguments()
                return arguments.getString(MESSAGE_KEY)
            }

        companion object {
            private const val MESSAGE_KEY = "message_key"
            fun newInstance(message: String): DialogFragment {
                val dialogFragment: DialogFragment = TroubleshootingDialog()
                dialogFragment.isCancelable = false
                val arguments = Bundle()
                arguments.putString(MESSAGE_KEY, message)
                dialogFragment.arguments = arguments
                return dialogFragment
            }
        }
    }
}