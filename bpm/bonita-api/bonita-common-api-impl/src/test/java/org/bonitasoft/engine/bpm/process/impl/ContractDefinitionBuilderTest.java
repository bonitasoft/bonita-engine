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
package org.bonitasoft.engine.bpm.process.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintType;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mvel2.MVEL;

@RunWith(MockitoJUnitRunner.class)
public class ContractDefinitionBuilderTest {

    private static final String name = "name";

    private static final Type type = Type.TEXT;

    private static final String description = "description";

    private static final String actorName = "actor name";

    @Mock
    ContractDefinitionImpl contractDefinitionImpl;

    @Mock
    private ProcessDefinitionBuilder processDefinitionBuilder;

    @Mock
    private FlowElementContainerDefinitionImpl container;

    private UserTaskDefinitionImpl activity;

    private ContractDefinitionBuilder contractDefinitionBuilder;

    @Before
    public void before() throws Exception {
        activity = new UserTaskDefinitionImpl(name, actorName);
        contractDefinitionBuilder = new ContractDefinitionBuilder(processDefinitionBuilder, container, activity);

    }

    @Test
    public void addContractOnProcess() throws Exception {
        //when
        ProcessDefinitionBuilder myProcess = new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0");
        myProcess.addContract();

        //then
        assertThat(myProcess.getProcess().getContract()).isNotNull();

    }

    @Test
    public void addInputTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addSimpleInput(name, type, description);

        //then
        assertThat(activity.getContract().getSimpleInputs()).as("should get 1 input").hasSize(1);
        assertThat(activity.getContract().getSimpleInputs().get(0).isMultiple()).as("should not be multiple").isFalse();
        checkBuilder(builder);

    }

    @Test
    public void addMultipleInputTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addSimpleInput(name, type, description, true);

        //then
        assertThat(activity.getContract().getSimpleInputs().get(0).isMultiple()).as("should be multiple").isTrue();
        checkBuilder(builder);

    }

    @Test
    public void addMultipleSimpleInputTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addSimpleInput(name, type, description);

        //then
        assertThat(activity.getContract().getSimpleInputs()).hasSize(1);
        checkBuilder(builder);

    }

    @Test
    public void addComplexInputTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addComplexInput(name, description, new ArrayList<SimpleInputDefinition>(), null);

        //then
        assertThat(activity.getContract().getComplexInputs()).hasSize(1);
        assertThat(activity.getContract().getComplexInputs().get(0).isMultiple()).as("should not be multiple").isFalse();
        checkBuilder(builder);

    }

    @Test
    public void addMultipleComplexInputTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addComplexInput(name, description, true, new ArrayList<SimpleInputDefinition>(),
                null);

        //then
        assertThat(activity.getContract().getComplexInputs()).hasSize(1);
        assertThat(activity.getContract().getComplexInputs().get(0).isMultiple()).as("should be multiple").isTrue();
        checkBuilder(builder);

    }

    @Test
    public void addFileComplexInputTest() throws Exception {
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addFileInput("document", "It is a simple document");

        final List<ComplexInputDefinition> complexInputs = activity.getContract().getComplexInputs();
        assertThat(complexInputs).hasSize(1);
        final List<SimpleInputDefinition> simpleInputs = complexInputs.get(0).getSimpleInputs();
        assertThat(simpleInputs).hasSize(2);
        assertThat(simpleInputs.get(0).getName()).isEqualTo("name");
        assertThat(simpleInputs.get(0).getType()).isEqualTo(Type.TEXT);
        assertThat(simpleInputs.get(1).getName()).isEqualTo("content");
        assertThat(simpleInputs.get(1).getType()).isEqualTo(Type.BYTE_ARRAY);

        checkBuilder(builder);
    }

    @Test
    public void addRuleTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addConstraint(name, "expression", "explanation", "inputName");

        //then
        assertThat(activity.getContract().getConstraints()).hasSize(1);
        checkBuilder(builder);

    }

    @Test
    public void addMandatoryConstraintTest() throws Exception {
        //when
        final ContractDefinitionBuilder builder = contractDefinitionBuilder.addMandatoryConstraint("inputName");

        //then
        assertThat(activity.getContract().getConstraints()).hasSize(1);
        final ConstraintDefinition definition = activity.getContract().getConstraints().get(0);
        assertThat(definition.getInputNames()).containsExactly("inputName");
        assertThat(definition.getExplanation()).as("bad explanation").isEqualTo("input inputName is mandatory");
        assertThat(definition.getConstraintType()).as("constraint should be a MANDATORY one").isEqualTo(ConstraintType.MANDATORY);

        checkBuilder(builder);
    }

    @Test
    public void addMandatoryConstraint_should_fail() throws Exception {
        //given
        final List<Object> failingValues = new ArrayList<Object>();
        failingValues.add(null);
        failingValues.add("");

        //when then
        checkConstraintShouldGiveExpectedResult(failingValues, false);
    }

    @Test
    public void addMandatoryConstraint_should_success() throws Exception {
        //given
        final List<Object> successValues = new ArrayList<Object>();
        successValues.add("not null value");
        successValues.add(0);
        successValues.add(new Date());

        //when then
        checkConstraintShouldGiveExpectedResult(successValues, true);
    }

    private void checkConstraintShouldGiveExpectedResult(final List<Object> values, final Boolean expectedResult) {
        for (final Object failingValue : values) {
            final Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("inputName", failingValue);

            //when
            contractDefinitionBuilder.addMandatoryConstraint("inputName");

            //then
            checkConstraintGivesExpectedResult(variables, expectedResult);
        }
    }

    private void checkConstraintGivesExpectedResult(final Map<String, Object> variables, final Boolean expectedResult) {
        final Boolean result = MVEL.evalToBoolean(activity.getContract().getConstraints().get(0).getExpression(), variables);
        assertThat(result).as("mandatory rule failure with value:" + variables.toString()).isEqualTo(expectedResult);
    }

    private void checkBuilder(final ContractDefinitionBuilder builder) {
        assertThat(builder).as("should return a builder").isNotNull().isEqualTo(contractDefinitionBuilder);
    }

}
