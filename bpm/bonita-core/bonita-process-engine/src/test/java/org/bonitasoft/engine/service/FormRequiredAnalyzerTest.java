/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class FormRequiredAnalyzerTest {

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @InjectMocks
    private FormRequiredAnalyzer formRequiredAnalyzer;

    @Test
    public void findActivityWithNameShouldReturnNullForNullTaskName() throws Exception {
        final UserTaskDefinition userTaskDefinition = formRequiredAnalyzer.findActivityWithName(
                Collections.<ActivityDefinition> singletonList(new UserTaskDefinitionImpl("tarea", "someActorName")), null);
        assertThat(userTaskDefinition).isNull();
    }

    @Test
    public void findActivityWithNameShouldReturnNullForNullActivityList() throws Exception {
        final UserTaskDefinition userTaskDefinition = formRequiredAnalyzer.findActivityWithName(null, "tarea");
        assertThat(userTaskDefinition).isNull();
    }

    @Test
    public void findActivityWithNameShouldReturnNullForTaskNotFoundForGivenName() throws Exception {
        final UserTaskDefinitionImpl expectedUserTaskDefinition = new UserTaskDefinitionImpl("tarea", "someActorName");
        final UserTaskDefinition userTaskDefinition = formRequiredAnalyzer.findActivityWithName(
                Collections.<ActivityDefinition> singletonList(expectedUserTaskDefinition), "some other name");
        assertThat(userTaskDefinition).isNull();
    }

    @Test
    public void findActivityWithNameShouldReturnUserTaskDefinitionForFoundTask() throws Exception {
        final UserTaskDefinitionImpl expectedUserTaskDefinition = new UserTaskDefinitionImpl("tarea", "someActorName");
        final UserTaskDefinition userTaskDefinition = formRequiredAnalyzer.findActivityWithName(
                Collections.<ActivityDefinition> singletonList(expectedUserTaskDefinition), "tarea");
        assertThat(userTaskDefinition).isEqualTo(expectedUserTaskDefinition);
    }

    @Test
    public void isFormRequiredShouldBeFalseForOverview() throws Exception {
        final boolean formRequired = formRequiredAnalyzer.isFormRequired(new SFormMappingImpl(1L, SFormMappingImpl.TYPE_PROCESS_OVERVIEW, null, ""));
        assertThat(formRequired).isFalse();
    }

    @Test
    public void isFormRequiredShouldBeFalseIfExceptionOccurs() throws Exception {
        doThrow(SProcessDefinitionNotFoundException.class).when(processDefinitionService).getDesignProcessDefinition(157L);
        final boolean formRequired = formRequiredAnalyzer.isFormRequired(new SFormMappingImpl(157L, SFormMappingImpl.TYPE_PROCESS_START, null, ""));
        assertThat(formRequired).isFalse();
    }

    @Test
    public void isFormRequiredShouldBeFalseIfNoContractOnProcessStart() throws Exception {
        final DesignProcessDefinition definition = mock(DesignProcessDefinition.class);
        doReturn(null).when(definition).getContract();
        doReturn(definition).when(processDefinitionService).getDesignProcessDefinition(111L);
        final boolean formRequired = formRequiredAnalyzer.isFormRequired(new SFormMappingImpl(111L, SFormMappingImpl.TYPE_PROCESS_START, null, ""));
        assertThat(formRequired).isFalse();
    }

    @Test
    public void isFormRequiredShouldBeTrueIfNonEmptyContractFoundOnProcessStart() throws Exception {
        final DesignProcessDefinition definition = mock(DesignProcessDefinition.class);
        final ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        contractDefinition.addInput(new InputDefinitionImpl("input1", "theInput"));
        doReturn(contractDefinition).when(definition).getContract();
        doReturn(definition).when(processDefinitionService).getDesignProcessDefinition(111L);
        final boolean formRequired = formRequiredAnalyzer.isFormRequired(new SFormMappingImpl(111L, SFormMappingImpl.TYPE_PROCESS_START, null, ""));
        assertThat(formRequired).isTrue();
    }

    @Test
    public void isFormRequiredShouldBeFalseIfEmptyContractFoundOnProcessStart() throws Exception {
        final DesignProcessDefinition definition = mock(DesignProcessDefinition.class);
        final ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        doReturn(contractDefinition).when(definition).getContract();
        doReturn(definition).when(processDefinitionService).getDesignProcessDefinition(111L);
        final boolean formRequired = formRequiredAnalyzer.isFormRequired(new SFormMappingImpl(111L, SFormMappingImpl.TYPE_PROCESS_START, null, ""));
        assertThat(formRequired).isFalse();
    }

    @Test
    public void isFormRequiredShouldBeTrueIfNonEmptyContractFoundOnTask() throws Exception {
        final DesignProcessDefinition definition = mock(DesignProcessDefinition.class);
        doReturn(mock(FlowElementContainerDefinition.class)).when(definition).getFlowElementContainer();
        doReturn(definition).when(processDefinitionService).getDesignProcessDefinition(111L);

        final FormRequiredAnalyzer spy = spy(formRequiredAnalyzer);
        final UserTaskDefinition userTaskDefinition = mock(UserTaskDefinition.class);
        final ContractDefinitionImpl contractDefinition = new ContractDefinitionImpl();
        contractDefinition.addInput(new InputDefinitionImpl("name", Type.BOOLEAN, "description"));
        doReturn(contractDefinition).when(userTaskDefinition).getContract();

        doReturn(userTaskDefinition).when(spy).findActivityWithName(anyList(), nullable(String.class));

        final boolean formRequired = spy.isFormRequired(new SFormMappingImpl(111L, SFormMappingImpl.TYPE_TASK, null, ""));
        assertThat(formRequired).isTrue();
    }

    @Test
    public void isFormRequiredShouldBeFalseIfEmptyContractFoundOnTask() throws Exception {
        final DesignProcessDefinition definition = mock(DesignProcessDefinition.class);
        doReturn(mock(FlowElementContainerDefinition.class)).when(definition).getFlowElementContainer();
        doReturn(definition).when(processDefinitionService).getDesignProcessDefinition(111L);

        final FormRequiredAnalyzer spy = spy(formRequiredAnalyzer);

        final boolean formRequired = spy.isFormRequired(new SFormMappingImpl(111L, SFormMappingImpl.TYPE_TASK, null, ""));
        assertThat(formRequired).isFalse();
    }
}
