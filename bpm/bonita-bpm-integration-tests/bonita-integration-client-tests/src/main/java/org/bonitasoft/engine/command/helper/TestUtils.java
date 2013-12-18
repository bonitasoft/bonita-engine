package org.bonitasoft.engine.command.helper;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.identity.User;

/**
 * Created by Vincent Elcrin
 * Date: 17/12/13
 * Time: 10:46
 */
public class TestUtils {

    private CommonAPITest testCase;

    public TestUtils(CommonAPITest testCase) {
        this.testCase = testCase;
    }

    public Process wrap(ProcessInstance process) {
        return new Process(process);
    }

    public class Process {

        private ProcessInstance process;

        Process(ProcessInstance process) {
            this.process = process;
        }

        public void execute(User behalf, String... steps) throws Exception {
            for (String step : steps) {
                testCase.waitForUserTaskAndExecuteIt(step, process.getId(), behalf.getId());
            }
        }

        public Expectation expect(String... steps) {
            return new Expectation(process, steps);
        }
    }

    public class Expectation {

        private ProcessInstance process;

        private String[] steps;

        Expectation(ProcessInstance process, String... steps) {
            this.process = process;
            this.steps = steps;
        }

        public void toBeStarted() throws Exception {
            for (String step : steps) {
                isStarted(step);
            }
        }

        private void isStarted(String step) throws Exception {
            try {
                testCase.waitForFlowNodeInReadyState(process, step, false);
            } catch (TimeoutException e) {
                fail(step + " is expected to be started.");
            }
        }
    }
}
