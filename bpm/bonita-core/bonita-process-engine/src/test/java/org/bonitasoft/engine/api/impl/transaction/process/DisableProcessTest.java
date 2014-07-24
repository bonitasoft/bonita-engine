/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.ArrayList;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DisableProcessTest {

    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private SchedulerService scheduler;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private SProcessDefinition processDefinition;
    @Mock
    private SFlowElementContainerDefinition flowElementCOntainerDefintion;

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.transaction.process.DisableProcess#execute()}.
     * Make sure we will always think to clean ClassloaderSrvice when disabling a process.
     * 
     * @throws SBonitaException
     */
    @Test
    public void testClassloaderClearedWhenExecuteCalled() throws SBonitaException {
        final long processDefinitionId = 1;
        Mockito.when(processDefinitionService.getProcessDefinition(processDefinitionId)).thenReturn(processDefinition);
        Mockito.when(processDefinition.getProcessContainer()).thenReturn(flowElementCOntainerDefintion);
        Mockito.when(flowElementCOntainerDefintion.getStartEvents()).thenReturn(new ArrayList<SStartEventDefinition>());
        final DisableProcess disableProcess = new DisableProcess(processDefinitionService, processDefinitionId, eventInstanceService, scheduler, logger,
                "testUserName", classLoaderService);

        disableProcess.execute();

        Mockito.verify(classLoaderService).removeLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
    }

}
