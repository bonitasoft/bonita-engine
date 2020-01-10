/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.junit.Before;
import org.junit.Test;

public class DeleteEventTriggerInstanceIT extends TestWithUser {

    @Before
    public void cleanTriggers() throws Exception {
        SQLUtils.execute("DELETE FROM event_trigger_instance");
    }

    @Test
    public void should_delete_timer_event_trigger_on_interrupted_process() throws Exception {
        ProcessDefinition procWithTimers = getProcessAPI()
                .deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("ProcWithTimers", "1.0")
                        .addStartEvent("start")
                        .addIntermediateCatchEvent("intermediate")
                        .addTimerEventTriggerDefinition(TimerType.DURATION, constant(100000))
                        .addTransition("start", "intermediate")
                        .getProcess());
        ProcessInstance processInstance = getProcessAPI().startProcess(procWithTimers.getId());
        await().until(() -> !getProcessAPI()
                .searchFlowNodeInstances(new SearchOptionsBuilder(0, 1).filter("name", "intermediate").done())
                .getResult().isEmpty());

        getProcessAPI().deleteProcessInstance(processInstance.getId());
        disableAndDeleteProcess(procWithTimers);

        assertThat(((Number) query("SELECT count(*) FROM event_trigger_instance").get(0)).intValue()).isEqualTo(0);
    }

    @Test
    public void should_delete_timer_event_of_aborted_boundary_event() throws Exception {
        ActorMapping actorMapping = new ActorMapping();
        Actor actor = new Actor("actor");
        actorMapping.addActor(actor);
        actor.addUser(user.getUserName());
        ProcessDefinition procWithTimers = getProcessAPI().deployAndEnableProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                        new ProcessDefinitionBuilder().createNewInstance("ProcWithBoundaryTimers", "1.0")
                                .addActor("actor")
                                .addUserTask("myTask", "actor").addBoundaryEvent("timerBoundary", true)
                                .addTimerEventTriggerDefinition(TimerType.DURATION, constant(100000))
                                .addUserTask("afterBoundary", "actor")
                                .addUserTask("boundaryOut", "actor")
                                .addTransition("timerBoundary", "boundaryOut")
                                .getProcess())
                        .setActorMapping(actorMapping)
                        .done());
        ProcessInstance processInstance = getProcessAPI().startProcess(procWithTimers.getId());

        long myTask = waitForUserTask("myTask");
        getProcessAPI().assignAndExecuteUserTask(user.getId(), myTask, Collections.emptyMap());
        long afterBoundary = waitForUserTask("afterBoundary");
        getProcessAPI().assignAndExecuteUserTask(user.getId(), afterBoundary, Collections.emptyMap());
        waitForProcessToFinish(processInstance.getId());
        assertThat(((Number) query("SELECT count(*) FROM event_trigger_instance").get(0)).intValue()).isEqualTo(0);

        disableAndDeleteProcess(procWithTimers);

        assertThat(((Number) query("SELECT count(*) FROM event_trigger_instance").get(0)).intValue()).isEqualTo(0);
    }

    private List query(String query) throws Exception {
        int t = 0;
        //retry because database locking issues might happen
        while (true) {
            t++;
            try {
                return SQLUtils.query(query);
            } catch (Exception e) {
                if (t > 5) {
                    throw e;
                }
            }
        }

    }

    private void deleteAllArchivedProcessInstances(ProcessDefinition processDefinition) throws Exception {
        getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 1000);
    }

    private Expression constant(long longValue) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantLongExpression(longValue);
    }
}
