// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.app.Application
import com.abbyy.rtr.ui.sample.datacapture.data.ProfileResultStorage

class App : Application() {
    companion object {
        lateinit var profileResultStorage: ProfileResultStorage
    }

    override fun onCreate() {
        super.onCreate()
        profileResultStorage = ProfileResultStorage(this)
    }
}
