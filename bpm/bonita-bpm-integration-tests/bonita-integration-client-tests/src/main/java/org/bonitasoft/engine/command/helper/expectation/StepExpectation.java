/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.command.helper.expectation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.CommonAPITest;
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

    private CommonAPITest testCase;

    private ProcessInstance process;

    private String[] steps;

    public StepExpectation(CommonAPITest testCase, ProcessInstance process, String... steps) {
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
            hasArchives(0, step, false);
        }
    }

    public void toBeExecuted(int times) throws SearchException {
        for (String step : steps) {
            hasArchives(times, step, true);
        }
    }

    private void isReady(String step) throws Exception {
        try {
            testCase.waitForFlowNodeInReadyState(process, step, false);
        } catch (TimeoutException e) {
            fail(step + " is expected to be started.");
        }
    }

    private void hasArchives(int times, String step, boolean terminal) throws SearchException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 0);
        builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, step);
        builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, process.getId());
        if(terminal) {
            builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL, true);
        }
        SearchResult<ArchivedFlowNodeInstance> result = testCase.getProcessAPI().searchArchivedFlowNodeInstances(builder.done());
        assertEquals(times, result.getCount());
    }


}
