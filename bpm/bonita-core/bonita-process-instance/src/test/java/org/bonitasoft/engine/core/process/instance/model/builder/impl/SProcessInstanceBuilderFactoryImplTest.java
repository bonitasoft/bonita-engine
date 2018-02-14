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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Date;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.junit.Test;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class SProcessInstanceBuilderFactoryImplTest {

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceBuilderFactoryImpl#createNewInstance(java.lang.String, long)}.
     */
    @Test
    public final void createNewInstance_with_name_and_process_definition_id() {
        // Given
        final SProcessInstanceBuilderFactoryImpl sProcessInstanceBuilderFactoryImpl = new SProcessInstanceBuilderFactoryImpl();
        final String name = "plop";
        final long processDefinitionId = 8L;
        final Date beforeStart = new Date();

        // When
        final SProcessInstanceBuilder sProcessInstanceBuilder = sProcessInstanceBuilderFactoryImpl.createNewInstance(name, processDefinitionId);

        // Then
        final SProcessInstance sProcessInstance = sProcessInstanceBuilder.done();
        assertEquals(name, sProcessInstance.getName());
        assertEquals(processDefinitionId, sProcessInstance.getProcessDefinitionId());
        final long startDate = sProcessInstance.getStartDate();
        assertTrue("The start date must be set, but it's equals to " + new Date(startDate), beforeStart.getTime() <= startDate);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceBuilderFactoryImpl#createNewInstance(java.lang.String, long, java.lang.String)}
     * .
     */
    @Test
    public final void createNewInstance_with_name_and_process_definition_id_and_description() {
        // Given
        final SProcessInstanceBuilderFactoryImpl sProcessInstanceBuilderFactoryImpl = new SProcessInstanceBuilderFactoryImpl();
        final String name = "plop";
        final long processDefinitionId = 8L;
        final Date beforeStart = new Date();
        final String description = "description";

        // When
        final SProcessInstanceBuilder sProcessInstanceBuilder = sProcessInstanceBuilderFactoryImpl.createNewInstance(name, processDefinitionId, description);

        // Then
        final SProcessInstance sProcessInstance = sProcessInstanceBuilder.done();
        assertEquals(name, sProcessInstance.getName());
        assertEquals(processDefinitionId, sProcessInstance.getProcessDefinitionId());
        assertEquals(description, sProcessInstance.getDescription());
        final long startDate = sProcessInstance.getStartDate();
        assertTrue("The start date must be set, but it's equals to " + new Date(startDate), beforeStart.getTime() <= startDate);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceBuilderFactoryImpl#createNewInstance(org.bonitasoft.engine.core.process.definition.model.SProcessDefinition)}
     * .
     */
    @Test
    public final void createNewInstance_with_process_definition() {
        // Given
        final SProcessInstanceBuilderFactoryImpl sProcessInstanceBuilderFactoryImpl = new SProcessInstanceBuilderFactoryImpl();
        final String name = "plop";
        final long processDefinitionId = 8L;
        final Date beforeStart = new Date();
        final String description = "description";
        final SProcessDefinition definition = mock(SProcessDefinition.class);
        doReturn(description).when(definition).getDescription();
        doReturn(processDefinitionId).when(definition).getId();
        doReturn(name).when(definition).getName();

        // When
        final SProcessInstanceBuilder sProcessInstanceBuilder = sProcessInstanceBuilderFactoryImpl.createNewInstance(definition);

        // Then
        final SProcessInstance sProcessInstance = sProcessInstanceBuilder.done();
        assertEquals(name, sProcessInstance.getName());
        assertEquals(processDefinitionId, sProcessInstance.getProcessDefinitionId());
        assertEquals(description, sProcessInstance.getDescription());
        final long startDate = sProcessInstance.getStartDate();
        assertTrue("The start date must be set, but it's equals to " + new Date(startDate), beforeStart.getTime() <= sProcessInstance.getStartDate());
    }
}
