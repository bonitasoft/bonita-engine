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
package org.bonitasoft.engine.bpm.contract.validation;

import static java.util.Arrays.asList;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractValidatorTest {

    private static final long PROCESS_DEFINITION_ID = 1245L;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ContractStructureValidator structureValidator;
    @Mock
    private ContractConstraintsValidator constraintValidator;
    @InjectMocks
    private ContractValidator contractValidator;

    private Map<String, Serializable> anyVariables() {
        return anyMap();
    }

    @Test
    public void should_not_validate_rules_if_structure_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> inputs = aMap().build();
        doThrow(new SContractViolationException("bad structure", new ArrayList<String>())).when(structureValidator).validate(contract, inputs);

        expectedException.expect(SContractViolationException.class);
        try {
            contractValidator.validate(PROCESS_DEFINITION_ID, contract, inputs);
        } finally {
            verify(constraintValidator, never()).validate(anyLong(), any(SContractDefinition.class), anyVariables());
        }

    }

    @Test
    public void should_return_false_if_structure_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        doThrow(new SContractViolationException("bad structure", new ArrayList<String>())).when(structureValidator).validate(contract, variables);

        expectedException.expect(SContractViolationException.class);
        contractValidator.validate(PROCESS_DEFINITION_ID, contract, variables);
    }

    @Test
    public void should_populate_comments_with_validation_problems_when_structure_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        final List<String> problems = Arrays.asList("There is problems with structure", "Might have issue with types too");
        doThrow(new SContractViolationException("bad structure", problems)).when(structureValidator).validate(contract, variables);

        expectedException.expect(new ExceptionHavingExplanations(problems));
        contractValidator.validate(PROCESS_DEFINITION_ID, contract, variables);
    }

    @Test
    public void should_return_false_if_rule_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        doThrow(new SContractViolationException("rule failure", new ArrayList<String>())).when(constraintValidator).validate(PROCESS_DEFINITION_ID, contract,
                variables);

        expectedException.expect(SContractViolationException.class);
        contractValidator.validate(PROCESS_DEFINITION_ID, contract, variables);
    }

    @Test
    public void should_populate_comments_with_validation_problems_when_rule_validation_fail() throws Exception {
        final SContractDefinition contract = aContract().build();
        final Map<String, Serializable> variables = aMap().build();
        final List<String> problems = asList("There is problems with a rule", "Might have issue with other rule too");
        doThrow(new SContractViolationException("rule failure", problems)).when(constraintValidator).validate(PROCESS_DEFINITION_ID, contract, variables);

        expectedException.expect(new ExceptionHavingExplanations(problems));
        contractValidator.validate(PROCESS_DEFINITION_ID, contract, variables);
    }

    private static class ExceptionHavingExplanations extends BaseMatcher<Exception> {

        private final List<String> problems;

        public ExceptionHavingExplanations(List<String> problems) {
            this.problems = problems;
        }

        @Override
        public boolean matches(Object item) {
            return (item instanceof SContractViolationException) && ((SContractViolationException) item).getExplanations().equals(problems);
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
