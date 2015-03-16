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
package org.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessManagementAPIImplDelegateTest {

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Spy
    private ProcessManagementAPIImplDelegate delegate;

    @Mock
    private SProcessDefinitionDeployInfo processInfo;

    @Mock
    private ClassLoaderService classLoaderService;

    @Before
    public void setUp() {
        doReturn(tenantServiceAccessor).when(delegate).getTenantAccessor();
        when(tenantServiceAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantServiceAccessor.getProcessInstanceService()).thenReturn(processInstanceService);
        when(tenantServiceAccessor.getClassLoaderService()).thenReturn(classLoaderService);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void purgeClassLoader_should_throw_an_exception_if_process_definition_does_not_exist() throws Exception {
        when(processDefinitionService.getProcessDeploymentInfo(45L)).thenThrow(new SProcessDefinitionNotFoundException("proc"));

        delegate.purgeClassLoader(45L);
    }

    @Test(expected = UpdateException.class)
    public void purgeClassLoader_should_throw_an_exception_if_process_definition_is_not_disable() throws Exception {
        when(processDefinitionService.getProcessDeploymentInfo(45L)).thenReturn(processInfo);
        when(processInfo.getActivationState()).thenReturn(ActivationState.ENABLED.toString());

        delegate.purgeClassLoader(45L);
    }

    @Test(expected = UpdateException.class)
    public void purgeClassLoader_should_throw_an_exception_if_process_instances_are_still_running() throws Exception {
        when(processDefinitionService.getProcessDeploymentInfo(45L)).thenReturn(processInfo);
        when(processInfo.getActivationState()).thenReturn(ActivationState.DISABLED.toString());
        when(processInstanceService.getNumberOfProcessInstances(45L)).thenReturn(3L);

        delegate.purgeClassLoader(45L);
    }

    @Test(expected = RetrieveException.class)
    public void purgeClassLoader_should_throw_an_exception_if_a_read_exception_occurs_when_retrieving_process_info() throws Exception {
        when(processDefinitionService.getProcessDeploymentInfo(45L)).thenThrow(new SProcessDefinitionReadException("error"));

        delegate.purgeClassLoader(45L);
    }

    @Test(expected = RetrieveException.class)
    public void purgeClassLoader_should_throw_an_exception_if_a_read_exception_occurs_when_retrieving_the_number_of_running_process_instances()
            throws Exception {
        when(processDefinitionService.getProcessDeploymentInfo(45L)).thenReturn(processInfo);
        when(processInfo.getActivationState()).thenReturn(ActivationState.DISABLED.toString());
        when(processInstanceService.getNumberOfProcessInstances(45L)).thenThrow(new SBonitaReadException("error"));

        delegate.purgeClassLoader(45L);
    }

    @Test
    public void purgeClassLoader_should_call_the_classLoader_service() throws Exception {
        final long processDefinitionId = 45L;
        when(processDefinitionService.getProcessDeploymentInfo(processDefinitionId)).thenReturn(processInfo);
        when(processInfo.getActivationState()).thenReturn(ActivationState.DISABLED.toString());
        when(processInstanceService.getNumberOfProcessInstances(processDefinitionId)).thenReturn(0L);

        delegate.purgeClassLoader(processDefinitionId);

        verify(classLoaderService).removeLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
    }

}
