package io.cucumber.core.runner;


import org.chef.cadt.model.DebugToolExecutionResult;
import org.chef.cadt.model.ErrorHandleResponse;
import org.chef.cadt.LocalDebugTool;
import org.chef.cadt.util.ConsoleColor;
import org.chef.cadt.util.GeneralUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import static io.cucumber.core.runner.ExecutionMode.SKIP;
import static io.cucumber.core.runner.TestStepResultStatus.from;
import static io.cucumber.messages.TimeConversion.javaDurationToDuration;
import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static io.cucumber.plugin.event.Status.PASSED;
import static java.time.Duration.ZERO;
import static org.chef.cadt.util.GeneralUtil.color;

abstract class TestStep implements io.cucumber.plugin.event.TestStep {
    private static boolean isEnded = false;

    private static final String[] TEST_ABORTED_OR_SKIPPED_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException",
            "org.opentest4j.TestAbortedException",
            "org.testng.SkipException",
            };

    static {
        Arrays.sort(TEST_ABORTED_OR_SKIPPED_EXCEPTIONS);
    }

    private final StepDefinitionMatch stepDefinitionMatch;
    private final UUID id;
    private TestCaseState caseState;
    private TestCase testCase;

    TestStep(UUID id, StepDefinitionMatch stepDefinitionMatch) {
        this.id = id;
        this.stepDefinitionMatch = stepDefinitionMatch;
    }

    @Override
    public String getCodeLocation() {
        return stepDefinitionMatch.getCodeLocation();
    }

    @Override
    public UUID getId() {
        return id;
    }

    void runQuietly() throws Throwable {
        stepDefinitionMatch.runStep(caseState);
    }

    String buildDesc() {
        String stepDescription;
        if (this instanceof HookTestStep) {
            final HookTestStep step = (HookTestStep) this;
            final String location = step.getDefinitionMatch().getHookDefinition().delegate
                    .getLocation();
            stepDescription = step.getHookType() + "-" + location;
        } else if (this instanceof PickleStepTestStep) {
            final PickleStepTestStep step = (PickleStepTestStep) this;
            stepDescription = step.getStepText();
        } else {
            stepDescription = "unknownStepType: " + getClass().getSimpleName();
        }
        return stepDescription;
    }

    ExecutionMode run(TestCase testCase, EventBus bus, TestCaseState state, ExecutionMode executionMode) {
        this.testCase = testCase;
        Instant startTime = bus.getInstant();
        emitTestStepStarted(testCase, bus, state.getTestExecutionId(), startTime);
        Status status;
        Throwable error = null;
        caseState = state;
        if (isLocalDebugMode()) {
            final DebugToolExecutionResult executionResult = localDebugExecuteStep(state, executionMode);
            error = executionResult.getError();
            status = executionResult.getStatus();
        } else {
            try {
                status = executeStep(state, executionMode);
            } catch (Throwable t) {
                error = t;
                status = mapThrowableToStatus(t);
            }
        }
        Instant stopTime = bus.getInstant();
        Duration duration = Duration.between(startTime, stopTime);
        Result result = mapStatusToResult(status, error, duration);
        state.add(result);

        emitTestStepFinished(testCase, bus, state.getTestExecutionId(), stopTime, duration, result);

        return result.getStatus().is(PASSED) ? executionMode : SKIP;
    }

    private DebugToolExecutionResult localDebugExecuteStep(TestCaseState state, ExecutionMode executionMode) {
        Status status;
        String stepDescription = buildDesc();
        System.out.println(color("Step Start: " + stepDescription, ConsoleColor.BLUE));
        while (true) {
            try {
                status = executeStep(state, executionMode);
                System.err.println("Step End: " + stepDescription);
                return new DebugToolExecutionResult(status, null);
            } catch (Throwable t) {
                if (!isEnded) {
                    t.printStackTrace();
                    GeneralUtil.sleep(100);
                    System.out.println("file location: " + getCodeLocation());
                    System.out.println("failed step: " + stepDescription);
                    final LocalDebugTool localDebugTool = new LocalDebugTool()
                            .withCaseFlow(((io.cucumber.core.runner.TestCase) testCase).caseFlow);
                    final ErrorHandleResponse response = localDebugTool.run();
                    switch (response) {
                        case REDO:
                            continue;
                        case RESUME:
                            return new DebugToolExecutionResult(PASSED, null);
                        case END:
                            isEnded = true;
                            break;
                        case END_STEP:
                        default:
                    }
                }
                return new DebugToolExecutionResult(mapThrowableToStatus(t), t);
            }
        }
    }

    private boolean isLocalDebugMode() {
        return LocalDebugTool.debugMode;
    }

    private void emitTestStepStarted(TestCase testCase, EventBus bus, UUID textExecutionId, Instant startTime) {
        bus.send(new TestStepStarted(startTime, testCase, this));
        bus.send(Messages.Envelope.newBuilder()
                 .setTestStepStarted(Messages.TestStepStarted.newBuilder()
                         .setTestCaseStartedId(textExecutionId.toString())
                         .setTestStepId(id.toString())
                         .setTimestamp(javaInstantToTimestamp(startTime)))
                 .build());
    }

    private Status executeStep(TestCaseState state, ExecutionMode executionMode) throws Throwable {
        state.setCurrentTestStepId(id);
        try {
            return executionMode.execute(stepDefinitionMatch, state);
        } finally {
            state.clearCurrentTestStepId();
        }
    }

    private Status mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class)) {
            return Status.PENDING;
        }
        if (Arrays.binarySearch(TEST_ABORTED_OR_SKIPPED_EXCEPTIONS, t.getClass().getName()) >= 0) {
            return Status.SKIPPED;
        }
        if (t.getClass() == UndefinedStepDefinitionException.class) {
            return Status.UNDEFINED;
        }
        if (t.getClass() == AmbiguousStepDefinitionsException.class) {
            return Status.AMBIGUOUS;
        }
        return Status.FAILED;
    }

    private Result mapStatusToResult(Status status, Throwable error, Duration duration) {
        if (status == Status.UNDEFINED) {
            return new Result(status, ZERO, null);
        }
        return new Result(status, duration, error);
    }

    private void emitTestStepFinished(
            TestCase testCase, EventBus bus, UUID textExecutionId, Instant stopTime, Duration duration, Result result
    ) {
        bus.send(new TestStepFinished(stopTime, testCase, this, result));
        Messages.TestStepFinished.TestStepResult.Builder builder = Messages.TestStepFinished.TestStepResult
                .newBuilder();

        if (result.getError() != null) {
            builder.setMessage(extractStackTrace(result.getError()));
        }
        Messages.TestStepFinished.TestStepResult testResult = builder.setStatus(from(result.getStatus()))
                .setDuration(javaDurationToDuration(duration))
                .build();
        bus.send(Messages.Envelope.newBuilder()
                .setTestStepFinished(Messages.TestStepFinished.newBuilder()
                        .setTestCaseStartedId(textExecutionId.toString())
                        .setTestStepId(id.toString())
                        .setTimestamp(javaInstantToTimestamp(stopTime))
                        .setTestStepResult(testResult))
                .build());
    }

    private String extractStackTrace(Throwable error) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(s);
        error.printStackTrace(printStream);
        return new String(s.toByteArray(), StandardCharsets.UTF_8);
    }

}
