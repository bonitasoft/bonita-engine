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
 */

package org.bonitasoft.engine.core.form.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class FormMappingKeyGeneratorImplTest {

    private static final long PROCESS_DEFINITION_ID = 123456l;
    private static final String PROCESS_NAME = "myProcess";
    private static final String PROCESS_VERSION = "12.589";
    @Mock
    private ProcessDefinitionService processDefinitionService;

    @InjectMocks
    private FormMappingKeyGeneratorImpl formMappingKeyGenerator;

    @Before
    public void before() throws Exception {
        SProcessDefinitionImpl toBeReturned = new SProcessDefinitionImpl(PROCESS_NAME, PROCESS_VERSION);

        doReturn(toBeReturned).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);
    }

    @Test
    public void generateKey_on_process_overview() throws Exception {
        String generateKey = formMappingKeyGenerator.generateKey(PROCESS_DEFINITION_ID, null, SFormMapping.TYPE_PROCESS_OVERVIEW);

        assertThat(generateKey).isEqualTo("processInstance/myProcess/12.589");
    }

    @Test(expected = SObjectCreationException.class)
    public void generateKey_for_un_unknown_type() throws Exception {
        formMappingKeyGenerator.generateKey(PROCESS_DEFINITION_ID, null, 12);
    }

    @Test
    public void generateKey_on_task() throws Exception {
        String generateKey = formMappingKeyGenerator.generateKey(PROCESS_DEFINITION_ID, "step1", SFormMapping.TYPE_TASK);

        assertThat(generateKey).isEqualTo("taskInstance/myProcess/12.589/step1");
    }

    @Test
    public void generateKey_on_process_start() throws Exception {
        String generateKey = formMappingKeyGenerator.generateKey(PROCESS_DEFINITION_ID, "step1", SFormMapping.TYPE_PROCESS_START);

        assertThat(generateKey).isEqualTo("process/myProcess/12.589");
    }

    @Test(expected = SObjectCreationException.class)
    public void generateKey_when_definition_not_found() throws Exception {
        formMappingKeyGenerator.generateKey(4444l, "step1", SFormMapping.TYPE_PROCESS_START);
    }

    @Test(expected = SObjectCreationException.class)
    public void generateKey_on_task_when_no_task_name() throws Exception {
        formMappingKeyGenerator.generateKey(PROCESS_DEFINITION_ID, null, SFormMapping.TYPE_TASK);
    }

    @Test(expected = SObjectCreationException.class)
    public void generateKeyShouldThrowObjectCreationExceptionWhenProcessDefNotFound() throws Exception {
        doThrow(SProcessDefinitionNotFoundException.class).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);
        formMappingKeyGenerator.generateKey(PROCESS_DEFINITION_ID, "someTaskName", SFormMapping.TYPE_TASK);
    }

}
