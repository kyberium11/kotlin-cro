// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.
package com.abbyy.rtr.ui.sample.datacapture.capture

import android.app.Application
import android.util.Log
import com.abbyy.mobile.rtr.Engine
import com.abbyy.rtr.ui.sample.datacapture.R

/**
 * Shared Engine holder which reuses an engine instance.
 * There is no need to create an engine every time.
 */
object SharedEngine {
    private const val LICENSE_FILE_NAME = "MCET01000007183074110325.ABBYY.License"
    private lateinit var ENGINE: Engine

    /**
     * Initializes Engine. If Engine is already initialized nothing happens.
     *
     * @param applicationContext Application instance is required to avoid Activity Context memory leak.
     *
     * @throws Throwable Throwable that may be thrown during initialization.
     * Use [EngineTroubleshootingDialog] to show error description.
     * @see EngineTroubleshootingDialog
     */
    @JvmStatic
	@Synchronized
    @Throws(Throwable::class)
    fun initialize(applicationContext: Application) {
        if (::ENGINE.isInitialized) {
            // An engine should be initialized only once during Application lifecycle.
            return
        }
        try {
            ENGINE = Engine.load(applicationContext, LICENSE_FILE_NAME)
        } catch (e: Throwable) {
            // Troubleshooting for the developer
            Log.e(
                applicationContext.getString(R.string.app_name),
                "Error loading ABBYY Mobile Capture SDK:",
                e
            )
            throw e
        }
    }

    @Synchronized
    fun get(): Engine {
        return ENGINE
    }
}