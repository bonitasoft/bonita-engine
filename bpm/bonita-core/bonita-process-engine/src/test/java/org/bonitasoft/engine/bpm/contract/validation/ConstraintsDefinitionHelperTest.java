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
package org.bonitasoft.engine.bpm.contract.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.aComplexInput;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.junit.Before;
import org.junit.Test;

public class ConstraintsDefinitionHelperTest {

    private ConstraintsDefinitionHelper constraintsDefinitionHelper;

    @Before
    public void before() throws Exception {
        constraintsDefinitionHelper = new ConstraintsDefinitionHelper();

    }

    @Test
    public void should_find_simple_definition() throws Exception {
        //given
        final SContractDefinition contract = buildContract();

        //when
        final String[] inputNames = { "user", "firstName", "lastName", "nature", "simple", "expenseLine", "date" };
        for (final String inputName : inputNames) {
            //then
            checkDefinitionIsFound(contract, inputName);
        }
    }

    @Test
    public void should_not_find_simple_definition() throws Exception {
        //given
        final SContractDefinition contract = buildContract();

        //when then
        assertThat(constraintsDefinitionHelper.getInputDefinition(contract, "not an input")).isNull();
    }

    private SContractDefinition buildContract() {
        final SContractDefinition contract = aContract()
                .withInput(
                        aComplexInput().withName("user").withInput(aSimpleInput(SType.TEXT).withName("firstName").build())
                                .withInput(aSimpleInput(SType.TEXT).withName("lastName").build()))
                .withInput(
                        aComplexInput()
                                .withName("expenseReport")
                                .withInput(
                                        aComplexInput().withName("expenseLine").withMultiple(true)
                                                .withInput(aSimpleInput(SType.TEXT).withName("nature").build())
                                                .withInput(aSimpleInput(SType.DECIMAL).withName("amount").build())
                                                .withInput(aSimpleInput(SType.DATE).withName("date").build())
                                                .withInput(aSimpleInput(SType.TEXT).withName("comment").build()).build()).build())
                .withInput(aSimpleInput(SType.TEXT).withMultiple(true).withName("simple").build())
                .build();
        return contract;
    }

    private void checkDefinitionIsFound(final SContractDefinition contract, final String inputName) {

        //when
        final SInputDefinition inputDefinition = constraintsDefinitionHelper.getInputDefinition(contract, inputName);

        //then
        assertThat(inputDefinition).as("should find definition for " + inputName).isNotNull();
        assertThat(inputDefinition.getName()).as("should find definition").isEqualTo(inputName);
    }
}
