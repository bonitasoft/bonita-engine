/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.command.helper.expectation;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.identity.User;

/**
 * Created by Vincent Elcrin
 * Date: 17/12/13
 * Time: 10:46
 */
public class TestUtils {

    private final CommonAPIIT testCase;

    public TestUtils(final CommonAPIIT testCase) {
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
                testCase.waitForUserTaskAndExecuteIt(process, step, behalf);
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
