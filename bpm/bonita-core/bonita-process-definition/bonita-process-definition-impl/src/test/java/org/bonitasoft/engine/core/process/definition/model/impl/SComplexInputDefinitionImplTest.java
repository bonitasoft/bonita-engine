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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.junit.Test;

public class SComplexInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";

    @Test
    public void construtor_should_initialize_members() throws Exception {
        //when
        final SComplexInputDefinitionImpl sComplexInputDefinitionImpl = new SComplexInputDefinitionImpl(NAME);

        //then
        assertThat(sComplexInputDefinitionImpl.isMultiple()).isFalse();
        assertThat(sComplexInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sComplexInputDefinitionImpl.getComplexInputDefinitions()).isNotNull().isEmpty();
        assertThat(sComplexInputDefinitionImpl.getSimpleInputDefinitions()).isNotNull().isEmpty();

    }

    @Test
    public void construtor_with_input_definition() throws Exception {
        //given
        final SSimpleInputDefinition name = new SSimpleInputDefinitionImpl("name", SType.TEXT, DESCRIPTION);

        final SSimpleInputDefinition city = new SSimpleInputDefinitionImpl("city", SType.TEXT, DESCRIPTION);
        final SSimpleInputDefinition zip = new SSimpleInputDefinitionImpl("zip", SType.INTEGER, DESCRIPTION);
        final SComplexInputDefinition adress = new SComplexInputDefinitionImpl("adress", DESCRIPTION, false, Arrays.asList(city, zip), null);

        //when
        final SComplexInputDefinitionImpl sComplexInputDefinitionImpl = new SComplexInputDefinitionImpl(NAME, DESCRIPTION, false, Arrays.asList(name),
                Arrays.asList(adress));

        //then
        assertThat(sComplexInputDefinitionImpl.isMultiple()).isFalse();
        assertThat(sComplexInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sComplexInputDefinitionImpl.getComplexInputDefinitions()).isNotEmpty().contains(adress);
        assertThat(sComplexInputDefinitionImpl.getSimpleInputDefinitions()).isNotEmpty().contains(name);

    }

    @Test
    public void construtor_with_inputDefinition() throws Exception {

        //when
        final SComplexInputDefinitionImpl sComplexInputDefinitionImpl = new SComplexInputDefinitionImpl(createComplexInputs());

        //then
        assertThat(sComplexInputDefinitionImpl.isMultiple()).isTrue();
        assertThat(sComplexInputDefinitionImpl.getName()).isEqualTo("expense");
        assertThat(sComplexInputDefinitionImpl.getDescription()).isEqualTo(DESCRIPTION);

        assertThat(sComplexInputDefinitionImpl.getComplexInputDefinitions()).as("should contain adress").isNotEmpty().hasSize(1);
        assertThat(sComplexInputDefinitionImpl.getSimpleInputDefinitions()).as("should contain name, amount and date").isNotEmpty().hasSize(3);

    }

    private InputDefinition createComplexInputs() {
        final InputDefinition name = new InputDefinitionImpl("name", Type.TEXT, DESCRIPTION);
        final InputDefinition amount = new InputDefinitionImpl("amount", Type.DECIMAL, DESCRIPTION);
        final InputDefinition date = new InputDefinitionImpl("date", Type.DATE, DESCRIPTION);

        final InputDefinition city = new InputDefinitionImpl("city", Type.TEXT, DESCRIPTION);
        final InputDefinition zip = new InputDefinitionImpl("zip", Type.INTEGER, DESCRIPTION);

        final InputDefinition adress = new InputDefinitionImpl("adress", DESCRIPTION, Arrays.asList(city, zip));

        final InputDefinition expense = new InputDefinitionImpl("expense", DESCRIPTION, true, Arrays.asList(name, amount, date, adress));
        return expense;
    }

}
