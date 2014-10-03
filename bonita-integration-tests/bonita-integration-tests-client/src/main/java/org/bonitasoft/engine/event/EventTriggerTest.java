/**
 * Copyright (C) 2014 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
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
public class EventTriggerTest extends AbstractEventTest {

    @Cover(classes = { EventTriggerInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "event trigger instance" }, jira = "BS-10439")
    @Test
    public void searchTimerEventTriggerInstances() throws Exception {
        final ProcessDefinition process1 = deployAndEnableSimpleProcess("Toto", "moi");
        final ProcessDefinition process2 = deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(90000, true, "Toto");
        final ProcessDefinition process3 = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessDefinition process4 = deployAndEnableProcessWithBoundarySignalEvent("signal");

        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process2.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(process3.getId());
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(process4.getId());

        waitForFlowNodeInState(processInstance2, "timer", TestStates.WAITING, true);
        waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance3);
        waitForUserTask("step1", processInstance4);

        // Return only timer event trigger
        final SearchOptions options = new SearchOptionsBuilder(0, 10).done();
        SearchResult<TimerEventTriggerInstance> searchTimerEventTriggerInstances = getProcessAPI().searchTimerEventTriggerInstances(
                processInstance2.getId(), options);
        assertEquals(1, searchTimerEventTriggerInstances.getCount());
        final List<TimerEventTriggerInstance> result = searchTimerEventTriggerInstances.getResult();
        assertEquals(1, result.size());
        assertEquals("timer", result.get(0).getEventInstanceName());

        searchTimerEventTriggerInstances = getProcessAPI().searchTimerEventTriggerInstances(processInstance3.getId(), options);
        assertEquals(0, searchTimerEventTriggerInstances.getCount());
        assertTrue(searchTimerEventTriggerInstances.getResult().isEmpty());

        searchTimerEventTriggerInstances = getProcessAPI().searchTimerEventTriggerInstances(processInstance4.getId(), options);
        assertEquals(0, searchTimerEventTriggerInstances.getCount());
        assertTrue(searchTimerEventTriggerInstances.getResult().isEmpty());

        disableAndDeleteProcess(process2, process1, process3, process4);
    }
}
