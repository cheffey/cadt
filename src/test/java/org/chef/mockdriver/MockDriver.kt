package org.chef.mockdriver;

import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.ios.IOSElement
import org.chef.cadt.util.ReflectUtil.setValue
import org.chef.mockdriver.util.ImageUtil
import org.openqa.selenium.*
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.SessionId
import java.util.UUID

class MockAndroidDriver : AndroidDriver<AndroidElement>(DesiredCapabilities()) {
    private val sessionId = SessionId("mock-sessionId-${UUID.randomUUID()}")

    override fun startSession(capabilities: Capabilities?) {
        val driverCapabilities = MutableCapabilities()
        driverCapabilities.setCapability("automationName","uiautomator2")
        setValue(this, "capabilities", driverCapabilities)
    }

    override fun getSessionId() = sessionId

    override fun <X> getScreenshotAs(outputType: OutputType<X>) = mockScreenshot(outputType)

    override fun findElements(by: String, using: String): MutableList<AndroidElement> = MockAndroidElement.mockElements()

    override fun findElements(by: By): MutableList<AndroidElement> = MockAndroidElement.mockElements()

    override fun findElement(by: String, using: String): AndroidElement = MockAndroidElement("mock0")

    private class MockAndroidElement(private val name: String) : AndroidElement() {
        override fun findElements(by: By): MutableList<AndroidElement> = mockElements()

        override fun findElement(by: By): MockAndroidElement = MockAndroidElement(name + "_child0")

        override fun <X> getScreenshotAs(outputType: OutputType<X>) = mockScreenshot(outputType)

        override fun click() {
            println("$name clicked")
        }

        override fun sendKeys(vararg keysToSend: CharSequence?) {
            println("sendKeys $keysToSend to $name")
        }

        override fun getAttribute(attributeName: String?): String = when (attributeName) {
            "resource-id" -> "$name-resource-id"
            "content-desc" -> "$name-content-desc"
            else -> "unsupported attribute: $attributeName"
        }

        override fun getText() = "$name-text"

        companion object {
            fun mockElements(): MutableList<AndroidElement> = mutableListOf(
                MockAndroidElement("mock0"), MockAndroidElement("mock1"),
                MockAndroidElement("mock2"), MockAndroidElement("mock3")
            )
        }
    }
}

class MockIOSDriver : IOSDriver<IOSElement>(DesiredCapabilities()) {
    private val sessionId = SessionId("mock-sessionId-${UUID.randomUUID()}")

    override fun startSession(capabilities: Capabilities?) {
        val driverCapabilities = MutableCapabilities()
        driverCapabilities.setCapability("automationName","xcuitest")
        setValue(this, "capabilities", driverCapabilities)
    }

    override fun getSessionId() = sessionId

    override fun <X> getScreenshotAs(outputType: OutputType<X>) = mockScreenshot(outputType)

    override fun findElements(by: String, using: String): MutableList<IOSElement> = MockIOSElement.mockElements()

    override fun findElements(by: By): MutableList<IOSElement> = MockIOSElement.mockElements()

    override fun findElement(by: String, using: String): IOSElement = MockIOSElement("mock0")

    private class MockIOSElement(private val name: String) : IOSElement() {
        override fun findElements(by: By): MutableList<IOSElement> = mockElements()

        override fun findElement(by: By): MockIOSElement = MockIOSElement(name + "_child0")

        override fun <X> getScreenshotAs(outputType: OutputType<X>) = mockScreenshot(outputType)

        override fun click() {
            println("$name clicked")
        }

        override fun sendKeys(vararg keysToSend: CharSequence?) {
            println("sendKeys $keysToSend to $name")
        }

        override fun getAttribute(attributeName: String?): String = when (attributeName) {
            "name" -> "$name-name"
            else -> "unsupported attribute: $attributeName"
        }

        override fun getText() = "$name-text"

        companion object {
            fun mockElements(): MutableList<IOSElement> = mutableListOf(
                MockIOSElement("mock0"), MockIOSElement("mock1"),
                MockIOSElement("mock2"), MockIOSElement("mock3")
            )
        }
    }
}

private fun <X> mockScreenshot(outputType: OutputType<X>): X {
    val base64 = ImageUtil.toBase64("/org/mockdriver/mockscreenshot.jpg")
    return outputType.convertFromBase64Png(base64)
}