/**
 * Copyright (C) 2014 BonitaSoft S.A.
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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.contract.validation.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.SContractDefinitionBuilder.aContract;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractValidatorTest {

    @Mock
    private ComplexContractStructureValidator structureValidator;

    @Mock
    private ContractRulesValidator rulesValidator;

    @InjectMocks
    private ContractValidator validator;

    private List<SRuleDefinition> anyRules() {
        return anyListOf(SRuleDefinition.class);
    }

    private Map<String, Object> anyInputs() {
        return anyMapOf(String.class, Object.class);
    }

    @Test
    public void should_have_an_empty_comments_list_if_validation_is_true() throws Exception {

        boolean valid = validator.isValid(aContract().build(), aMap().build());

        assertThat(valid).isTrue();
        assertThat(validator.getComments()).isEmpty();
    }

    @Test
    public void should_not_validate_rules_if_structure_validation_fail() throws Exception {
        SContractDefinition contract = aContract().build();
        Map<String, Object> inputs = aMap().build();
        doThrow(new ContractViolationException("bad structure", new ArrayList<String>()))
                .when(structureValidator).validate(contract, inputs);

        validator.isValid(contract, inputs);

        verify(rulesValidator, never()).validate(anyRules(), anyInputs());
    }

    @Test
    public void should_return_false_if_structure_validation_fail() throws Exception {
        SContractDefinition contract = aContract().build();
        Map<String, Object> inputs = aMap().build();
        doThrow(new ContractViolationException("bad structure", new ArrayList<String>()))
                .when(structureValidator).validate(contract, inputs);

        boolean valid = validator.isValid(contract, inputs);

        assertThat(valid).isFalse();
    }

    @Test
    public void should_populate_comments_with_validation_problems_when_structure_validation_fail() throws Exception {
        SContractDefinition contract = aContract().build();
        Map<String, Object> inputs = aMap().build();
        List<String> problems = Arrays.asList("There is problems with structure", "Might have issue with types too");
        doThrow(new ContractViolationException("bad structure", problems))
                .when(structureValidator).validate(contract, inputs);

        validator.isValid(contract, inputs);

        assertThat(validator.getComments()).isEqualTo(problems);
    }

    @Test
    public void should_return_false_if_rule_validation_fail() throws Exception {
        SContractDefinition contract = aContract().build();
        Map<String, Object> inputs = aMap().build();
        doThrow(new ContractViolationException("rule failure", new ArrayList<String>()))
                .when(rulesValidator).validate(contract.getRules(), inputs);

        boolean valid = validator.isValid(contract, inputs);

        assertThat(valid).isFalse();
    }

    @Test
    public void should_populate_comments_with_validation_problems_when_rule_validation_fail() throws Exception {
        SContractDefinition contract = aContract().build();
        Map<String, Object> inputs = aMap().build();
        List<String> problems = asList("There is problems with a rule", "Might have issue with other rule too");
        doThrow(new ContractViolationException("rule failure", problems))
                .when(rulesValidator).validate(contract.getRules(), inputs);

        validator.isValid(contract, inputs);

        assertThat(validator.getComments()).isEqualTo(problems);
    }
}
