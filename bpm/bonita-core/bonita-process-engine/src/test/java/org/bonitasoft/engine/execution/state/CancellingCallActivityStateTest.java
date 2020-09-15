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
package org.bonitasoft.engine.execution.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.archive.BPMArchiverService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class CancellingCallActivityStateTest {

    private static final long CALL_ACTIVITY_ID = 665437L;
    private static final long PROCESS_DEFINITION_ID = 4538938902L;
    private static final long PROCESS_INSTANCE_ID = 3240213094L;

    private SProcessDefinition processDefinition = new SProcessDefinitionImpl("myProcess", "1.0");
    private SProcessInstance processInstance = new SProcessInstance("myProcess", PROCESS_DEFINITION_ID);
    private SCallActivityInstance callActivity = new SCallActivityInstance("callACtivity", 5342985348L, 4323264L,
            65222L, 87686L, 2342L);
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private TechnicalLoggerService loggerService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    ProcessInstanceInterruptor processInstanceInterruptor;
    @Mock
    BPMArchiverService bpmArchiverService;

    @InjectMocks
    private CancellingCallActivityState cancellingCallActivityState;

    @Before
    public void before() throws Exception {
        callActivity.setId(CALL_ACTIVITY_ID);
        processInstance.setId(PROCESS_INSTANCE_ID);
    }

    @Test
    public void should_warn_when_completing_call_activity_with_no_called_process() throws Exception {
        //given
        callActivity.setTokenCount(0);
        doThrow(SProcessInstanceNotFoundException.class).when(processInstanceService)
                .getChildOfActivity(CALL_ACTIVITY_ID);
        //when
        cancellingCallActivityState.shouldExecuteState(processDefinition, callActivity);
        //then
        verify(loggerService).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                contains("No target process instance found when archiving the call activity"));
    }

    @Test
    public void shouldExecuteState_should_return_false_when_called_process_is_finished() throws Exception {
        //given
        callActivity.setTokenCount(0);
        doReturn(processInstance).when(processInstanceService).getChildOfActivity(CALL_ACTIVITY_ID);
        //when
        boolean shouldExecuteState = cancellingCallActivityState.shouldExecuteState(processDefinition, callActivity);
        //then
        assertThat(shouldExecuteState).isFalse();
    }

    @Test
    public void shouldExecuteState_should_return_true_when_called_process_is_not_finished() throws Exception {
        //given
        callActivity.setTokenCount(1);
        doReturn(processInstance).when(processInstanceService).getChildOfActivity(CALL_ACTIVITY_ID);
        //when
        boolean shouldExecuteState = cancellingCallActivityState.shouldExecuteState(processDefinition, callActivity);
        //then
        assertThat(shouldExecuteState).isTrue();
    }

    @Test
    public void should_archive_process_if_finished() throws Exception {
        //given
        callActivity.setTokenCount(0);
        doReturn(processInstance).when(processInstanceService).getChildOfActivity(CALL_ACTIVITY_ID);
        //when
        cancellingCallActivityState.shouldExecuteState(processDefinition, callActivity);
        //then
        verify(bpmArchiverService).archiveAndDeleteProcessInstance(processInstance);
    }

}
