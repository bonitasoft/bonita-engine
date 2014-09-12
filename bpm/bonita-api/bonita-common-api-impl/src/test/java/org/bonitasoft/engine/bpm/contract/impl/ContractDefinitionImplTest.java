package org.bonitasoft.engine.bpm.contract.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.junit.Before;
import org.junit.Test;

public class ContractDefinitionImplTest {

    private ContractDefinitionImpl contractDefinitionImpl;
    private InputDefinition expenseType;
    private InputDefinition expenseAmount;
    private InputDefinition expenseDate;
    private ComplexInputDefinition complexInput;

    @Before
    public void Before() {
        contractDefinitionImpl = new ContractDefinitionImpl();
        expenseType = new InputDefinitionImpl("expenseType", Type.TEXT, "describe expense type");
        expenseAmount = new InputDefinitionImpl("amount", Type.DECIMAL, "expense amount");
        expenseDate = new InputDefinitionImpl("date", Type.DATE, "expense date");
        complexInput = new ComplexInputDefinitionImpl("expense item", "description", Arrays.asList(expenseType, expenseAmount,
                expenseDate));

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
        contractDefinitionImpl.addComplexInput(complexInput);

        //then
        final List<InputDefinition> inputs = contractDefinitionImpl.getInputs();
        assertThat(inputs).as("should contains 1 complex type").hasSize(1);
    }
}
