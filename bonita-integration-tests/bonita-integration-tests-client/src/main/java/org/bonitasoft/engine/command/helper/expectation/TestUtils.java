package org.bonitasoft.engine.command.helper.expectation;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.identity.User;

/**
 * Created by Vincent Elcrin
 * Date: 17/12/13
 * Time: 10:46
 */
public class TestUtils {

    private final CommonAPITest testCase;

    public TestUtils(final CommonAPITest testCase) {
        this.testCase = testCase;
    }

    public Process wrap(final ProcessInstance process) {
        return new Process(process);
    }

    public class Process {

        private final ProcessInstance process;

        Process(final ProcessInstance process) {
            this.process = process;
        }

        public void execute(final User behalf, final String... steps) throws Exception {
            for (final String step : steps) {
                testCase.waitForUserTaskAndExecuteIt(step, process, behalf);
            }
        }

        public StepExpectation expect(final String... steps) {
            return new StepExpectation(testCase, process, steps);
        }

        public ProcessExpectation isExpected() {
            return new ProcessExpectation(testCase, process);
        }

        public VariableExpectation expectVariable(final String name) {
            return new VariableExpectation(testCase, process, name);
        }

        public DocumentExpectation expectDocument(final String name) {
            return new DocumentExpectation(testCase, process, name);
        }

        public void sendSignal(final String name) throws SendEventException {
            testCase.getProcessAPI().sendSignal(name);
        }
    }
}
