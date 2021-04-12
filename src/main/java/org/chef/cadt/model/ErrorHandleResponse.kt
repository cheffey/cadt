package org.chef.cadt.model

import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import io.cucumber.plugin.event.Status
import org.chef.cadt.util.ExceptionUtil.tryOrNull
import org.chef.cadt.util.ExceptionUtil.tryRunIgnoredException
import org.chef.cadt.util.GeneralUtil.color
import org.chef.cadt.util.ReflectUtil.setValue
import org.chef.cadt.util.StringUtil
import org.chef.cadt.util.ThrowableRunnable
import org.openqa.selenium.WebElement

/**
 * Created by Chef.Xie
 */

enum class ErrorHandleResponse {
    REDO,     //redo the caught failed step, if the step pass, it will continue the test flow and run next step
    END,      //report the caught failed step as failed, and it WON'T start LocalDebugTool for next failed step
    END_STEP, //report the caught failed step as failed, and it WILL start LocalDebugTool for next failed step
    RESUME    //report the caught failed step as success, it will continue the test flow and run next step
}







