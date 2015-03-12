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

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractValidatorTest {

    @Mock
    private ContractStructureValidator structureValidator;

    @Mock
    private ContractConstraintsValidator constraintValidator;

    @InjectMocks
    private ContractValidator contractValidator;

    private Map<String, Serializable> anyVariables() {
        return anyMapOf(String.class, Serializable.class);
    }

    @Test
    public void should_have_an_empty_comments_list_if_validation_is_true() throws Exception {

        final boolean valid = contractValidator.isValid(aContract().build(), aMap().build());

        assertThat(valid).isTrue();
        assertThat(contractValidator.getComments()).isEmpty();
    }

    @Test
    public void should_not_validate_rules_if_structure_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> inputs = aMap().build();
        doThrow(new ContractViolationException("bad structure", new ArrayList<String>()))
        .when(structureValidator).validate(contract, inputs);

        contractValidator.isValid(contract, inputs);

        verify(constraintValidator, never()).validate(any(SContractDefinition.class), anyVariables());
    }

    @Test
    public void should_return_false_if_structure_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        doThrow(new ContractViolationException("bad structure", new ArrayList<String>()))
        .when(structureValidator).validate(contract, variables);

        final boolean valid = contractValidator.isValid(contract, variables);

        assertThat(valid).isFalse();
    }

    @Test
    public void should_populate_comments_with_validation_problems_when_structure_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        final List<String> problems = Arrays.asList("There is problems with structure", "Might have issue with types too");
        doThrow(new ContractViolationException("bad structure", problems))
        .when(structureValidator).validate(contract, variables);

        contractValidator.isValid(contract, variables);

        assertThat(contractValidator.getComments()).isEqualTo(problems);
    }

    @Test
    public void should_return_false_if_rule_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        doThrow(new ContractViolationException("rule failure", new ArrayList<String>()))
        .when(constraintValidator).validate(contract, variables);

        final boolean valid = contractValidator.isValid(contract, variables);

        assertThat(valid).isFalse();
    }

    @Test
    public void should_populate_comments_with_validation_problems_when_rule_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        final List<String> problems = asList("There is problems with a rule", "Might have issue with other rule too");
        doThrow(new ContractViolationException("rule failure", problems))
        .when(constraintValidator).validate(contract, variables);

        contractValidator.isValid(contract, variables);

        assertThat(contractValidator.getComments()).isEqualTo(problems);
    }

}
