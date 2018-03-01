/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SStartEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.STimerEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class TimerEventHandlerStrategyTest {

    private static final long PROCESS_DEFINITION_ID = 5642367345L;
    private static final long SUB_PROCESS_ID = 84233523563L;
    private static final long PROCESS_INSTANCE_ID = 15908L;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private ExpressionResolverService expressionResolverService;
    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private TechnicalLoggerService logger;
    @InjectMocks
    private TimerEventHandlerStrategy timerEventHandlerStrategy;
    private SProcessDefinitionImpl processDefinition;
    private SEventDefinitionImpl eventDefintion;
    private STimerEventTriggerDefinitionImpl eventTriggerDefinition;
    private SProcessInstanceImpl processInstance;
    private SExpressionImpl timerExpression;
    @Captor
    private ArgumentCaptor<SJobDescriptor> jobDescriptorArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<SJobParameter>> listArgumentCaptor;
    @Captor
    private ArgumentCaptor<Trigger> triggerArgumentCaptor;

    @Before
    public void before() throws Exception {
        processDefinition = new SProcessDefinitionImpl("test", "1.0");
        processDefinition.setId(PROCESS_DEFINITION_ID);
        eventDefintion = new SStartEventDefinitionImpl(6543721L, "startevent");
        timerExpression = new SExpressionImpl("duration", "12342", "constant", "long", "", Collections.<SExpression> emptyList());

        processInstance = new SProcessInstanceImpl("proceInst", PROCESS_DEFINITION_ID);
        processInstance.setId(PROCESS_INSTANCE_ID);

    }

    @Test
    public void should_trigger_timer_for_event_subprocess_evaluate_trigger_using_process_instance_context() throws Exception {
        doReturn(5000L).when(expressionResolverService).evaluate(timerExpression,
                new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS_INSTANCE", PROCESS_DEFINITION_ID));
        eventTriggerDefinition = new STimerEventTriggerDefinitionImpl(STimerType.DURATION, timerExpression);
        //when
        timerEventHandlerStrategy.handleEventSubProcess(processDefinition, eventDefintion, eventTriggerDefinition, SUB_PROCESS_ID, processInstance);
        //then
        verify(expressionResolverService).evaluate(timerExpression, new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS_INSTANCE", PROCESS_DEFINITION_ID));
    }

    @Test
    public void should_trigger_timer_with_expected_duration() throws Exception {
        doReturn(5000L).when(expressionResolverService).evaluate(timerExpression,
                new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS_INSTANCE", PROCESS_DEFINITION_ID));
        eventTriggerDefinition = new STimerEventTriggerDefinitionImpl(STimerType.DURATION, timerExpression);
        //when
        timerEventHandlerStrategy.handleEventSubProcess(processDefinition, eventDefintion, eventTriggerDefinition, SUB_PROCESS_ID, processInstance);
        long now = System.currentTimeMillis();
        //then
        verify(schedulerService).schedule(jobDescriptorArgumentCaptor.capture(), listArgumentCaptor.capture(), triggerArgumentCaptor.capture());
        assertThat(triggerArgumentCaptor.getValue()).isInstanceOf(OneShotTrigger.class);
        assertThat(triggerArgumentCaptor.getValue().getStartDate()).isBetween(new Date(now - 200), new Date(now + 5200));
    }

    @Test
    public void should_trigger_timer_with_expected_cycle() throws Exception {
        doReturn("theCron").when(expressionResolverService).evaluate(timerExpression,
                new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS_INSTANCE", PROCESS_DEFINITION_ID));
        eventTriggerDefinition = new STimerEventTriggerDefinitionImpl(STimerType.CYCLE, timerExpression);
        //when
        timerEventHandlerStrategy.handleEventSubProcess(processDefinition, eventDefintion, eventTriggerDefinition, SUB_PROCESS_ID, processInstance);
        long now = System.currentTimeMillis();
        //then
        verify(schedulerService).schedule(jobDescriptorArgumentCaptor.capture(), listArgumentCaptor.capture(), triggerArgumentCaptor.capture());
        assertThat(triggerArgumentCaptor.getValue()).isInstanceOf(UnixCronTrigger.class);
        assertThat(((UnixCronTrigger) triggerArgumentCaptor.getValue()).getExpression()).isEqualTo("theCron");
    }

    @Test
    public void should_trigger_timer_with_expected_date() throws Exception {
        Date startDate = new Date(4893705);
        doReturn(startDate).when(expressionResolverService).evaluate(timerExpression,
                new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS_INSTANCE", PROCESS_DEFINITION_ID));
        eventTriggerDefinition = new STimerEventTriggerDefinitionImpl(STimerType.DATE, timerExpression);
        //when
        timerEventHandlerStrategy.handleEventSubProcess(processDefinition, eventDefintion, eventTriggerDefinition, SUB_PROCESS_ID, processInstance);
        long now = System.currentTimeMillis();
        //then
        verify(schedulerService).schedule(jobDescriptorArgumentCaptor.capture(), listArgumentCaptor.capture(), triggerArgumentCaptor.capture());
        assertThat(triggerArgumentCaptor.getValue()).isInstanceOf(OneShotTrigger.class);
        assertThat(triggerArgumentCaptor.getValue().getStartDate()).isEqualTo(startDate);
    }
}
