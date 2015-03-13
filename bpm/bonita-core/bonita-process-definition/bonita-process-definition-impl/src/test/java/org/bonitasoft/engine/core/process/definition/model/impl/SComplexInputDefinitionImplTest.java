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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
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

    private ComplexInputDefinition createComplexInputs() {
        final SimpleInputDefinition name = new SimpleInputDefinitionImpl("name", Type.TEXT, DESCRIPTION);
        final SimpleInputDefinition amount = new SimpleInputDefinitionImpl("amount", Type.DECIMAL, DESCRIPTION);
        final SimpleInputDefinition date = new SimpleInputDefinitionImpl("date", Type.DATE, DESCRIPTION);

        final SimpleInputDefinition city = new SimpleInputDefinitionImpl("city", Type.TEXT, DESCRIPTION);
        final SimpleInputDefinition zip = new SimpleInputDefinitionImpl("zip", Type.INTEGER, DESCRIPTION);

        final ComplexInputDefinition adress = new ComplexInputDefinitionImpl("adress", DESCRIPTION, Arrays.asList(city, zip), null);

        final ComplexInputDefinition expense = new ComplexInputDefinitionImpl("expense", DESCRIPTION, true, Arrays.asList(name, amount, date),
                Arrays.asList(adress));
        return expense;
    }

}
