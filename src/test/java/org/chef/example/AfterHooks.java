package org.chef.example;

import org.chef.Context;
import org.openqa.selenium.WebDriver;

import io.cucumber.java.After;

public class AfterHooks {
    private final WebDriver driver = Context.getDriver();

    @After(order = 1)
    public void after1() {
        driver.quit();
    }
}
