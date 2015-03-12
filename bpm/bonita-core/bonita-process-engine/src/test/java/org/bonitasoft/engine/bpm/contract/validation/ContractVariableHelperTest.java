package org.bonitasoft.engine.bpm.contract.validation;

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintType;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.junit.Before;
import org.junit.Test;

public class ContractVariableHelperTest {

    private ContractVariableHelper contractVariableHelper;

    @Before
    public void before() {
        contractVariableHelper = new ContractVariableHelper();
    }

    @Test
    public void should_find_multiple_contract_variables() throws Exception {
        final Map<String, Serializable> variables = buildMap();

        final SConstraintDefinition constraintDefinition = getConstraintDefinitionWithName("nature");

        final List<Map<String, Serializable>> buildMandatoryMultipleInputVariables = contractVariableHelper.buildMandatoryMultipleInputVariables(
                constraintDefinition, variables);
        assertThat(buildMandatoryMultipleInputVariables).as("should not find variable for constraint " + constraintDefinition.getName()).hasSize(2);

    }

    @Test
    public void should_not_find_multiple_contract_variables() throws Exception {
        final Map<String, Serializable> variables = buildMap();

        final SConstraintDefinition constraintDefinition = getConstraintDefinitionWithName("not in payload");

        final List<Map<String, Serializable>> buildMandatoryMultipleInputVariables = contractVariableHelper.buildMandatoryMultipleInputVariables(
                constraintDefinition, variables);
        assertThat(buildMandatoryMultipleInputVariables).as("should not find variable for constraint " + constraintDefinition.getName()).isEmpty();

    }

    @Test
    public void should_convert_multiple_variable() throws Exception {
        //given
        final List<String> values = Arrays.asList("item1", "item2", "item3");
        final Map<String, Serializable> variable = aMap().put("key", (Serializable) values).build();

        //when
        final List<Map<String, Serializable>> convertMultipleToList = contractVariableHelper.convertMultipleToList(variable);

        //then
        assertThat(convertMultipleToList).as("should split multiple item").hasSize(3);
        for (int i = 0; i < convertMultipleToList.size(); i++) {
            final Map<String, Serializable> map = convertMultipleToList.get(i);
            assertThat(map).as("should contain key").hasSize(1);
            assertThat(map.get("key").toString()).as("should contain item" + i).isEqualTo("item" + (i + 1));
        }
    }

    @Test
    public void should_not_convert_multiple_variable_and_return_empty_list() throws Exception {
        //given
        final Map<String, Serializable> variable = aMap().put("key", "not a list").build();

        //when
        final List<Map<String, Serializable>> convertMultipleToList = contractVariableHelper.convertMultipleToList(variable);

        //then
        assertThat(convertMultipleToList).as("should not split multiple item").isNotNull().isEmpty();
    }

    private Map<String, Serializable> buildMap() {
        final Map<String, Serializable> variables;
        final Map<String, Serializable> user = aMap().put("firstName", "john").put("lastName", "doe").build();
        final Map<String, Serializable> taxiExpenseLine = aMap().put("nature", "taxi").put("amount", 30).put("date", "2014-10-16").put("comment", "slow")
                .build();
        final Map<String, Serializable> hotelExpenseLine = aMap().put("nature", "hotel").put("amount", 1000).put("date", "2014-10-16")
                .put("comment", "expensive")
                .build();
        final List<Map<String, Serializable>> expenseLines = new ArrayList<Map<String, Serializable>>();
        expenseLines.add(taxiExpenseLine);
        expenseLines.add(hotelExpenseLine);
        final Map<String, Serializable> expenseReport = aMap().put("expenseReport", (Serializable) expenseLines).build();

        variables = aMap().put("user", (Serializable) user).put("expenseReport", (Serializable) expenseReport).build();
        return variables;
    }

    private SConstraintDefinition getConstraintDefinitionWithName(final String inputName) {
        final SConstraintDefinition constraintDefinition = new SConstraintDefinitionImpl(inputName, "fake", "fake", SConstraintType.MANDATORY);
        constraintDefinition.getInputNames().add(inputName);
        return constraintDefinition;
    }
}
