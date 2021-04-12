package org.chef.cadt.model

import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import io.cucumber.plugin.event.Status
import org.chef.cadt.exception.WithUselessTraceException
import org.chef.cadt.util.ExceptionUtil
import org.chef.cadt.util.ReflectUtil
import org.chef.cadt.util.ThrowableRunnable
import org.openqa.selenium.WebElement

/**
 * Created by Chef.Xie
 */

class Device(val driver: AppiumDriver<*>) {
    private val capabilities = driver.capabilities
    val appiumUrl = driver.remoteAddress
    fun sessionId() = ExceptionUtil.tryOrNull { driver.sessionId?.toString() }
        ?: "Undetermined, may Disconnected"

    fun reconnect() {
        ExceptionUtil.tryRunIgnoredException { driver.quit() }
        val newDriver: AppiumDriver<WebElement> = when (driver) {
            is AndroidDriver -> AndroidDriver<WebElement>(appiumUrl, capabilities)
            is IOSDriver -> IOSDriver<WebElement>(appiumUrl, capabilities)
            else -> TODO()
        }
        val newSessionId = newDriver.sessionId
        ReflectUtil.setValue(driver, "sessionId", newSessionId)
    }

    fun automationType(): AutomationType {
        val automation = driver.capabilities.getCapability("automationName").toString()
        return matchType(automation) ?: throw WithUselessTraceException("Unsupported automationName: $automation")
    }

    private fun matchType(automationName: String): AutomationType? {
        for (type in AutomationType.values()) {
            if (type.name.equals(automationName, true))
                return type
        }
        return null
    }
}

enum class AutomationType {
    XCUITEST, UIAUTOMATOR2, ESPRESSO
}

data class DebugToolExecutionResult(var status: Status, var error: Throwable?)

class StepInfo(val description: String, private val testStep: ThrowableRunnable) {
    fun execute() {
        testStep.run()
    }

    override fun toString(): String {
        return "description: $description"
    }
}
