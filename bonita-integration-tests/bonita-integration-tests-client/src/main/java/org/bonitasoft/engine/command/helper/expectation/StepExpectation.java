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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

/**
 * @author Vincent Elcrin
 */
public class StepExpectation {

    private CommonAPIIT testCase;

    private ProcessInstance process;

    private String[] steps;

    public StepExpectation(CommonAPIIT testCase, ProcessInstance process, String... steps) {
        this.testCase = testCase;
        this.process = process;
        this.steps = steps;
    }

    public void toBeReady() throws Exception {
        for (String step : steps) {
            isReady(step);
        }
    }

    public void toNotHaveArchives() throws Exception {
        for (String step : steps) {
            assertEquals(0, getArchives(step, false, null).getCount());
        }
    }

    public void toBeExecuted(int times) throws SearchException {
        for (String step : steps) {
            assertEquals(times, getArchives(step, true, "completed").getCount());
        }
    }

    public void toBeAborted() throws SearchException {
        for (String step : steps) {
            assertEquals(1, getArchives(step, true, "aborted").getCount());
        }
    }

    private void isReady(String step) throws Exception {
        try {
            testCase.waitForFlowNodeInReadyState(process, step, false);
        } catch (TimeoutException e) {
            fail(step + " is expected to be started.");
        }
    }


    private SearchResult<ArchivedFlowNodeInstance> getArchives(String step, boolean terminal, String state) throws SearchException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 0);
        builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, step);
        builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, process.getId());
        if(terminal) {
            builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL, true);
        }
        if(state != null) {
            builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, state);
        }
        return testCase.getProcessAPI().searchArchivedFlowNodeInstances(builder.done());
    }
}
