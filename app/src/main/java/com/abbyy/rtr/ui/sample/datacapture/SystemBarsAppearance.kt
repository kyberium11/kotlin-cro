// ABBYY Mobile Capture © 2020 ABBYY Development, Inc.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.rtr.ui.sample.datacapture

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi

@Suppress("DEPRECATION")
class SystemBarsAppearance(private val window: Window) {

    @ColorInt private var savedStatusBarColor: Int = Color.BLACK
    @ColorInt private var savedNavigationBarColor: Int = Color.BLACK
    @RequiresApi(Build.VERSION_CODES.M) private var savedSystemUiVisibility: Int = 0
    @RequiresApi(Build.VERSION_CODES.R) private var savedSystemBarsAppearance: Int = 0

    fun apply() {
        savedStatusBarColor = window.statusBarColor
        savedNavigationBarColor = window.navigationBarColor
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            savedSystemUiVisibility = decorView.systemUiVisibility
            decorView.systemUiVisibility = decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            savedSystemBarsAppearance = decorView.windowInsetsController?.systemBarsAppearance ?: 0
            val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            decorView.windowInsetsController?.setSystemBarsAppearance(0, mask)
        }
    }

    fun restore() {
        window.statusBarColor = savedStatusBarColor
        window.navigationBarColor = savedNavigationBarColor

        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility = savedSystemUiVisibility
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.windowInsetsController
                ?.setSystemBarsAppearance(savedSystemBarsAppearance, savedSystemBarsAppearance)
        }
    }

}
