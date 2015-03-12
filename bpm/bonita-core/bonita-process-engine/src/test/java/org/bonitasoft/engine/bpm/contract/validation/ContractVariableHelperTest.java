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
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.aMap;

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
        final Map<String, Object> variables = buildMap();

        final SConstraintDefinition constraintDefinition = getConstraintDefinitionWithName("nature");

        final List<Map<String, Object>> buildMandatoryMultipleInputVariables = contractVariableHelper.buildMandatoryMultipleInputVariables(
                constraintDefinition, variables);
        assertThat(buildMandatoryMultipleInputVariables).as("should not find variable for constraint " + constraintDefinition.getName()).hasSize(2);

    }

    @Test
    public void should_not_find_multiple_contract_variables() throws Exception {
        final Map<String, Object> variables = buildMap();

        final SConstraintDefinition constraintDefinition = getConstraintDefinitionWithName("not in payload");

        final List<Map<String, Object>> buildMandatoryMultipleInputVariables = contractVariableHelper.buildMandatoryMultipleInputVariables(
                constraintDefinition, variables);
        assertThat(buildMandatoryMultipleInputVariables).as("should not find variable for constraint " + constraintDefinition.getName()).isEmpty();

    }

    @Test
    public void should_convert_multiple_variable() throws Exception {
        //given
        final List<String> values = Arrays.asList("item1", "item2", "item3");
        final Map<String, Object> variable = aMap().put("key", values).build();

        //when
        final List<Map<String, Object>> convertMultipleToList = contractVariableHelper.convertMultipleToList(variable);

        //then
        assertThat(convertMultipleToList).as("should split multiple item").hasSize(3);
        for (int i = 0; i < convertMultipleToList.size(); i++) {
            final Map<String, Object> map = convertMultipleToList.get(i);
            assertThat(map).as("should contain key").hasSize(1);
            assertThat(map.get("key").toString()).as("should contain item" + i).isEqualTo("item" + (i + 1));
        }
    }

    @Test
    public void should_not_convert_multiple_variable_and_return_empty_list() throws Exception {
        //given
        final Map<String, Object> variable = aMap().put("key", "not a list").build();

        //when
        final List<Map<String, Object>> convertMultipleToList = contractVariableHelper.convertMultipleToList(variable);

        //then
        assertThat(convertMultipleToList).as("should not split multiple item").isNotNull().isEmpty();
    }

    private Map<String, Object> buildMap() {
        final Map<String, Object> variables;
        final Map<String, Object> user = aMap().put("firstName", "john").put("lastName", "doe").build();
        final Map<String, Object> taxiExpenseLine = aMap().put("nature", "taxi").put("amount", 30).put("date", "2014-10-16").put("comment", "slow").build();
        final Map<String, Object> hotelExpenseLine = aMap().put("nature", "hotel").put("amount", 1000).put("date", "2014-10-16").put("comment", "expensive")
                .build();
        final List<Map<String, Object>> expenseLines = new ArrayList<Map<String, Object>>();
        expenseLines.add(taxiExpenseLine);
        expenseLines.add(hotelExpenseLine);
        final Map<String, Object> expenseReport = aMap().put("expenseReport", expenseLines).build();

        variables = aMap().put("user", user).put("expenseReport", expenseReport).build();
        return variables;
    }

    private SConstraintDefinition getConstraintDefinitionWithName(final String inputName) {
        final SConstraintDefinition constraintDefinition = new SConstraintDefinitionImpl(inputName, "fake", "fake", SConstraintType.MANDATORY);
        constraintDefinition.getInputNames().add(inputName);
        return constraintDefinition;
    }
}
