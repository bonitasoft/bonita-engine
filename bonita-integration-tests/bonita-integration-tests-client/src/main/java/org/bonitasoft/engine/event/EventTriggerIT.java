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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class EventTriggerIT extends AbstractEventIT {

    @Cover(classes = { EventTriggerInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "event trigger instance" }, jira = "BS-10439")
    @Test
    public void searchTimerEventTriggerInstances() throws Exception {
        final ProcessDefinition process1 = deployAndEnableSimpleProcess("Toto", "moi");
        final ProcessDefinition process2 = deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(7200000L, false, "Toto");
        final ProcessDefinition process3 = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessDefinition process4 = deployAndEnableProcessWithBoundarySignalEvent("signal");

        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process2.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(process3.getId());
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(process4.getId());

        waitForFlowNodeInState(processInstance2, "timer", TestStates.WAITING, true);
        waitForUserTask(processInstance3, PARENT_PROCESS_USER_TASK_NAME);
        waitForUserTask(processInstance4, "step1");

        // Return only timer event trigger
        SearchOptions options = new SearchOptionsBuilder(0, 10).done();
        SearchResult<TimerEventTriggerInstance> searchTimerEventTriggerInstances = getProcessAPI().searchTimerEventTriggerInstances(processInstance3.getId(),
                options);
        assertEquals(0, searchTimerEventTriggerInstances.getCount());
        assertTrue(searchTimerEventTriggerInstances.getResult().isEmpty());

        searchTimerEventTriggerInstances = getProcessAPI().searchTimerEventTriggerInstances(processInstance4.getId(), options);
        assertEquals(0, searchTimerEventTriggerInstances.getCount());
        assertTrue(searchTimerEventTriggerInstances.getResult().isEmpty());

        options = new SearchOptionsBuilder(0, 10).filter(TimerEventTriggerInstanceSearchDescriptor.EVENT_INSTANCE_NAME, "timer").done();
        searchTimerEventTriggerInstances = getProcessAPI().searchTimerEventTriggerInstances(processInstance2.getId(), options);
        assertEquals(1, searchTimerEventTriggerInstances.getCount());
        final List<TimerEventTriggerInstance> result = searchTimerEventTriggerInstances.getResult();
        assertEquals(1, result.size());
        assertEquals("timer", result.get(0).getEventInstanceName());

        disableAndDeleteProcess(process2, process1, process3, process4);
    }

    @Cover(classes = { EventTriggerInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "event trigger instance", "update" }, jira = "BS-10439")
    @Test
    public void updateTimerEventTriggerInstance() throws Exception {
        final ProcessDefinition process1 = deployAndEnableSimpleProcess("Toto2", "moi");
        final ProcessDefinition process2 = deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(7200000L, false, "Toto2");
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process2.getId());
        try {
            waitForFlowNodeInState(processInstance2, "timer", TestStates.WAITING, true);

            final SearchOptions options = new SearchOptionsBuilder(0, 10).done();
            final List<TimerEventTriggerInstance> result = getProcessAPI().searchTimerEventTriggerInstances(processInstance2.getId(), options).getResult();
            assertEquals(1, result.size());

            final Date date = new Date();
            final Date newDate = getProcessAPI().updateExecutionDateOfTimerEventTriggerInstance(result.get(0).getId(), date);
            assertTrue(newDate.equals(date) || newDate.after(date));

            waitForUserTask(processInstance2, EXCEPTION_STEP);
        } finally {
            disableAndDeleteProcess(process2, process1);
        }
    }
}
