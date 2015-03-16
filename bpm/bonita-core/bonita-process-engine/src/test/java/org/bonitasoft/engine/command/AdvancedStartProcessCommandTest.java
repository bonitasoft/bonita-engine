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
package org.bonitasoft.engine.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



/**
 * @author Elias Ricken de Medeiros
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedStartProcessCommandTest {
    
    private static final long PROCESS_DEFINITION_ID = 1234L;

    @Mock
    private TenantServiceAccessor serviceAccessor;
    
    @Mock
    private ProcessDefinitionService processDefinitionService;
    
    @Mock
    private SProcessDefinition procDef;
    
    @Mock
    private SFlowElementContainerDefinition container;

    @Mock
    private SBoundaryEventDefinition boundary;

    @Mock
    private SUserTaskDefinition userTask;
    
    private Map<String, Serializable> parameters;
    
    @Before
    public void setUp() throws Exception {
        parameters = new HashMap<String, Serializable>(2);
        parameters.put(AdvancedStartProcessCommand.PROCESS_DEFINITION_ID, PROCESS_DEFINITION_ID);
        parameters.put(AdvancedStartProcessCommand.STARTED_BY, 123L);
        parameters.put(AdvancedStartProcessCommand.ACTIVITY_NAME, "");
        
        Set<SFlowNodeDefinition> flowNodes = new HashSet<SFlowNodeDefinition>();
        flowNodes.add(userTask);
        flowNodes.add(boundary);
        
        doReturn(processDefinitionService).when(serviceAccessor).getProcessDefinitionService();
        doReturn(procDef).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);
        doReturn(container).when(procDef).getProcessContainer();
        doReturn(flowNodes).when(container).getFlowNodes();
    }
    
    @Test
    public void execute_command_throws_SCommandExecutionException_if_validation_returns_problems() throws Exception {
        AdvancedStartProcessCommand command = new AdvancedStartProcessCommand();
        try {
            command.execute(parameters, serviceAccessor);
            fail("As the activity names is empty and exception must be thrown during the validation");
        } catch (SCommandExecutionException e) {
            assertThat(e.getMessage()).contains("No flownode named '' was found in the process");
        }
    }

}
