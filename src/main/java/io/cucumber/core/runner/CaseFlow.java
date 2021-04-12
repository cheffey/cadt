package io.cucumber.core.runner;

import org.chef.cadt.model.StepInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Chef.Xie
 */
public class CaseFlow {
    public final List<TestStep> allSteps;
    public int currentStepIndex = 0;

    public CaseFlow(List<TestStep> allSteps) {
        this.allSteps = allSteps;
    }

    public List<StepInfo> allStepInfos() {
        return allSteps.stream().map(this::buildStepInfo).collect(Collectors.toList());
    }

    private StepInfo buildStepInfo(TestStep testStep) {
        return new StepInfo(testStep.buildDesc(), testStep::runQuietly);
    }

}
