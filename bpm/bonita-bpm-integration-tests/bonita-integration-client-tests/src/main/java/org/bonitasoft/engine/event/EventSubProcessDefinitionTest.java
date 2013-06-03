/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class EventSubProcessDefinitionTest {

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces cannot have incomming transitions")
    public void eventSubProcessMusntHaveIncommingTransitions() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        builder.addTransition("step1", "eventSubProcess");
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces cannot have outgoing transitions")
    public void eventSubProcessMusntHaveOutgoingTransitions() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        builder.addUserTask("step2", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        builder.addTransition("eventSubProcess", "step2");
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces mus have a start event with a trigger")
    public void eventSubProcessMustHaveTrigger() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("start");
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("start", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces mus have a start event with a trigger")
    public void eventSubProcessCannotHaveMoreThanOneStartEvent() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart1").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        subProcessBuilder.addStartEvent("timerStart2").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(2000));
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart1", "subStep");
        subProcessBuilder.addTransition("timerStart2", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    // FIXME remove after adding ids in the flow nodes
    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that all names are unique")
    public void eventSubProcessCannotHaveTwoFlowNodesWithTheSameName() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart1").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        subProcessBuilder.addUserTask("step1", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart1", "step1");
        subProcessBuilder.addTransition("step1", "endSubProcess");
        builder.done();
    }

}
