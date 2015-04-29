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
package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.junit.Before;
import org.junit.Test;

public class ContractDefinitionImplTest {

    private ContractDefinitionImpl contractDefinitionImpl;
    private InputDefinition expenseType;
    private InputDefinition expenseAmount;
    private InputDefinition expenseDate;
    private InputDefinition complexInput;

    @Before
    public void Before() {
        contractDefinitionImpl = new ContractDefinitionImpl();
        expenseType = new InputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        expenseAmount = new InputDefinitionImpl("amount", Type.DECIMAL, "expense amount");
        expenseDate = new InputDefinitionImpl("date", Type.DATE, "expense date");

        complexInput = new InputDefinitionImpl("expense item", "description", Arrays.asList(expenseType, expenseDate, expenseAmount));

    }

    @Test
    public void addInputTest() throws Exception {
        //when
        contractDefinitionImpl.addInput(expenseType);
        contractDefinitionImpl.addInput(expenseAmount);
        contractDefinitionImpl.addInput(expenseDate);

        //then
        final List<InputDefinition> inputs = contractDefinitionImpl.getInputs();
        assertThat(inputs).as("should contains 3 inputs").hasSize(3);
    }

    @Test
    public void addComplexInputTest() throws Exception {
        //when
        contractDefinitionImpl.addInput(complexInput);

        //then
        final List<InputDefinition> inputs = contractDefinitionImpl.getInputs();
        assertThat(inputs).as("should contains 1 complex type").hasSize(1);
    }
}
