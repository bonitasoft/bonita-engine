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

        public StepExpectation expect(String... steps) {
            return new StepExpectation(testCase, process, steps);
        }

        public ProcessExpectation isExpected() {
            return new ProcessExpectation(testCase, process);
        }

        public VariableExpectation expectVariable(String name) {
            return new VariableExpectation(testCase, process, name);
        }

        public DocumentExpectation expectDocument(String name) {
            return new DocumentExpectation(testCase, process, name);
        }

        public void sendSignal(String name) throws SendEventException {
            testCase.getProcessAPI().sendSignal(name);
        }
    }
}
