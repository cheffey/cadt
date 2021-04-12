package org.chef

import io.appium.java_client.AppiumDriver
import org.chef.mockdriver.MockAndroidDriver
import org.chef.mockdriver.MockIOSDriver
import org.openqa.selenium.*

const val DEBUG_MODE = true

object Context {
    private val drivers:List<AppiumDriver<out WebElement>> = listOf(MockAndroidDriver(), MockIOSDriver())

    @JvmStatic
    fun getDrivers(): List<AppiumDriver<out WebElement>> {
        return drivers
    }
}