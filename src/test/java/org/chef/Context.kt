package org.chef

import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.DesiredCapabilities

const val DEBUG_MODE = true

object Context {
    private val __drivers by lazy { listOf(connect()) }

    @JvmStatic
    fun getDrivers(): List<WebDriver> {
        return __drivers
    }

    @JvmStatic
    fun getDriver(): WebDriver {
        return __drivers[0]
    }

    fun connect(): WebDriver {
        System.setProperty("webdriver.chrome.driver", "D:\\tools\\chromedriver.exe")
        val caps = DesiredCapabilities()
        // caps.setCapability(NEW_COMMAND_TIMEOUT, 1200)
        return ChromeDriver()
    }
}

fun main() {
    Context.connect()
}