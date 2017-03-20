package uk.tanton.streaming.live.cucumber;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/feature")
public class CucumberRunnerTest {
    public void main() {}

}
