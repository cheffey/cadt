package org.chef.example;

import org.chef.Context;
import org.chef.ContextKt;
import org.chef.cadt.LocalDebugTool;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import io.cucumber.java.Before;

public class BeforeHooks {
    private final WebDriver driver = Context.getDriver();

    @Before(order = 1)
    public void preset() {
        LocalDebugTool.debugMode = ContextKt.DEBUG_MODE;
        LocalDebugTool.driverLoader = Context::getDrivers;
    }

    @Before(order = 2)
    public void login() {
        driver.get("https://account.cnblogs.com/signin");
        driver.findElement(By.xpath("//*[@autocomplete='username']"))
              .sendKeys("YourDa");
        driver.findElement(By.xpath("//*[@autocomplete='current-password']"))
              .sendKeys("wrongPassword");
        driver.findElement(By.xpath("//*[text()=' 登录 ']"))
              .click();
        throw new RuntimeException("login fail");
    }
}
