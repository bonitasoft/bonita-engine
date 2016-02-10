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
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.junit.Test;

public class SInputDefinitionImplTest {

    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";

    @Test
    public void contructor_with_name() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, SType.TEXT, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).isFalse();
    }

    @Test
    public void constructor_with_input_definition() throws Exception {
        //given
        final InputDefinition simpleInput = new InputDefinitionImpl(NAME, Type.TEXT, DESCRIPTION, true);

        //when
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(simpleInput);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).isTrue();
        assertThat(sInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sInputDefinitionImpl.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(sInputDefinitionImpl.getType()).isEqualTo(SType.TEXT);

    }

    @Test
    public void construtor_should_initialize_members() throws Exception {
        //when
        final SInputDefinitionImpl sComplexInputDefinitionImpl = new SInputDefinitionImpl(NAME, "");

        //then
        assertThat(sComplexInputDefinitionImpl.isMultiple()).isFalse();
        assertThat(sComplexInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sComplexInputDefinitionImpl.getInputDefinitions()).isNotNull().isEmpty();

    }

    @Test
    public void construtor_with_input_definition() throws Exception {
        //given
        final SInputDefinition name = new SInputDefinitionImpl("name", SType.TEXT, DESCRIPTION);

        final SInputDefinition city = new SInputDefinitionImpl("city", SType.TEXT, DESCRIPTION);
        final SInputDefinition zip = new SInputDefinitionImpl("zip", SType.INTEGER, DESCRIPTION);
        final SInputDefinition adress = new SInputDefinitionImpl("adress", DESCRIPTION, false, Arrays.asList(city, zip));

        //when
        final SInputDefinitionImpl sComplexInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION, false, Arrays.asList(name,adress));

        //then
        assertThat(sComplexInputDefinitionImpl.isMultiple()).isFalse();
        assertThat(sComplexInputDefinitionImpl.getName()).isEqualTo(NAME);
        assertThat(sComplexInputDefinitionImpl.getInputDefinitions()).isNotEmpty().containsExactly(name, adress);

    }

    @Test
    public void construtor_with_inputDefinition() throws Exception {

        //when
        final SInputDefinitionImpl sComplexInputDefinitionImpl = new SInputDefinitionImpl(createComplexInputs());

        //then
        assertThat(sComplexInputDefinitionImpl.isMultiple()).isTrue();
        assertThat(sComplexInputDefinitionImpl.getName()).isEqualTo("expense");
        assertThat(sComplexInputDefinitionImpl.getDescription()).isEqualTo(DESCRIPTION);

        assertThat(sComplexInputDefinitionImpl.getInputDefinitions()).as("should contain name, amount and date").isNotEmpty().hasSize(4);

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

    @Test
    public void constructor_without_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should not be multiple").isFalse();

    }

    @Test
    public void constructor_with_description_without_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should not be multiple").isFalse();

    }

    @Test
    public void constructor_with_multiple() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, SType.BOOLEAN, DESCRIPTION, true);

        //then
        assertThat(sInputDefinitionImpl.isMultiple()).as("should be multiple").isTrue();

    }

    @Test
    public void constructor_with_name_and_description() throws Exception {
        //given
        final SInputDefinitionImpl sInputDefinitionImpl = new SInputDefinitionImpl(NAME, DESCRIPTION);

        //then
        assertThat(sInputDefinitionImpl.getName()).as("should get name").isEqualTo(NAME);
        assertThat(sInputDefinitionImpl.getDescription()).as("should get name").isEqualTo(DESCRIPTION);

    }

}
