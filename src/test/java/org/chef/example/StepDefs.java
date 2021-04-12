package org.chef.example;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;

import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class StepDefs {

    @Given("^Fails (\\d+)$")
    public void fails(int i) {
        throw new RuntimeException(i + "");
    }

    @When("^Success$")
    public void success() {
    }
}