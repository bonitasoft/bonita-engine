package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.junit.Before;
import org.junit.Test;

public class ContractDefinitionImplTest {

    private ContractDefinitionImpl contractDefinitionImpl;
    private SimpleInputDefinition expenseType;
    private SimpleInputDefinition expenseAmount;
    private SimpleInputDefinition expenseDate;
    private ComplexInputDefinition complexInput;

    @Before
    public void Before() {
        contractDefinitionImpl = new ContractDefinitionImpl();
        expenseType = new SimpleInputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        expenseAmount = new SimpleInputDefinitionImpl("amount", Type.DECIMAL, "expense amount");
        expenseDate = new SimpleInputDefinitionImpl("date", Type.DATE, "expense date");

        complexInput = new ComplexInputDefinitionImpl("expense item", "description", Arrays.asList(expenseType, expenseDate, expenseAmount), null);

    }

    @Test
    public void addInputTest() throws Exception {
        //when
        contractDefinitionImpl.addSimpleInput(expenseType);
        contractDefinitionImpl.addSimpleInput(expenseAmount);
        contractDefinitionImpl.addSimpleInput(expenseDate);

        //then
        final List<SimpleInputDefinition> inputs = contractDefinitionImpl.getSimpleInputs();
        assertThat(inputs).as("should contains 3 inputs").hasSize(3);
    }

    @Test
    public void addComplexInputTest() throws Exception {
        //when
        contractDefinitionImpl.addComplexInput(complexInput);

        //then
        final List<ComplexInputDefinition> inputs = contractDefinitionImpl.getComplexInputs();
        assertThat(inputs).as("should contains 1 complex type").hasSize(1);
    }
}
