/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.contractInputMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SConstraintDefinitionBuilder.aRuleFor;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyMap;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractConstraintsValidatorTest {

    private static final String NICE_COMMENT = "no way!";
    private static final String COMMENT = "comment";
    private static final String IS_VALID = "isValid";
    private static final long PROCESS_DEFINITION_ID = 154L;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    @Mock
    private ExpressionService expressionService;

    private ContractConstraintsValidator validator;

    @Before
    public void setUp() throws SExpressionTypeUnknownException, SExpressionDependencyMissingException,
            SExpressionEvaluationException,
            SInvalidExpressionException {
        doReturn(false).when(expressionService).evaluate(any(SExpression.class), anyMap(), anyMap(),
                any(ContainerState.class));
        returnTrueForExpressionWithContent("isValid != null");
        returnTrueForExpressionWithContent("isValid || !isValid && comment != null");
        validator = new ContractConstraintsValidator(expressionService);
    }

    private void returnTrueForExpressionWithContent(String content)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        doReturn(true).when(expressionService).evaluate(expressionHavingContent(content), anyMap(),
                anyMap(), any(ContainerState.class));
    }

    private SExpression expressionHavingContent(String content) {
        return argThat(expr -> expr.getContent().equals(content));
    }

    @Test
    public void should_log_all_rules_in_debug_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();

        systemOutRule.clearLog();
        validator.validate(PROCESS_DEFINITION_ID, contract,
                contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));

        //then
        String[] log = systemOutRule.getLog().split("\n");
        assertThat(log).anyMatch(l -> l.matches(".*DEBUG.*Evaluating constraint.*"));
        assertThat(systemOutRule.getLog()).doesNotContain("WARN");
    }

    @Test
    public void should_not_log_all_rules_in_info_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();

        //
        systemOutRule.clearLog();
        validator.validate(PROCESS_DEFINITION_ID, contract,
                contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));

        //then
        String[] log = systemOutRule.getLog().split("\n");
        assertThat(log).noneMatch(l -> l.matches(".*INFO.*Evaluating constraint.*"));
        assertThat(systemOutRule.getLog()).doesNotContain("WARN");
    }

    @Test
    public void isValid_should_log_invalid_constraints_in_warning_mode() throws Exception {
        //given
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(BOOLEAN).withName(IS_VALID).build())
                .withConstraint(aRuleFor(IS_VALID).name("false constraint").expression("false")
                        .explanation("should re implement").build())
                .build();

        try {
            systemOutRule.clearLog();
            validator.validate(PROCESS_DEFINITION_ID, contract,
                    contractInputMap(entry(IS_VALID, false), entry(COMMENT, null)));
            fail("validation should fail");
        } catch (final SContractViolationException e) {
            assertThat(systemOutRule.getLog()).contains(
                    "Constraint [false constraint] on input(s) [isValid] is not valid");
        }
    }

    @Test
    public void validate_should_throw_ContractViolation_with_explanations_when_rule_fails_to_evaluate()
            throws Exception {
        //given
        final SContractDefinition contract = buildContractWithInputsAndConstraints();
        final SConstraintDefinitionImpl badRule = new SConstraintDefinitionImpl("bad rule", "a == b", "failing rule");
        contract.getConstraints().add(badRule);

        //when
        try {
            validator.validate(PROCESS_DEFINITION_ID, contract,
                    contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));
            fail("validation should fail");
        } catch (final SContractViolationException e) {
            assertThat(e.getExplanations()).hasSize(1).containsExactly(badRule.getExplanation());
        }

    }

    @Test
    public void exception_during_evaluation_report_it() throws Exception {
        //given
        final SContractDefinition contract = buildContractWithInputsAndConstraints();
        doThrow(SExpressionEvaluationException.class).when(expressionService).evaluate(any(SExpression.class), anyMap(),
                anyMap(), any(ContainerState.class));

        //when
        try {
            validator.validate(PROCESS_DEFINITION_ID, contract,
                    contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));
            fail("validation should fail");
        } catch (final SContractViolationException e) {
            assertThat(e.getMessage()).contains("Exception while");
            assertThat(e.getCause()).isNotNull().isInstanceOf(SExpressionEvaluationException.class);
        }
    }

    private SContractDefinition buildContractWithInputsAndConstraints() {
        return aContract()
                .withInput(aSimpleInput(BOOLEAN).withName(IS_VALID).build())
                .withInput(aSimpleInput(BOOLEAN).withName(IS_VALID).build())
                .withConstraint(aRuleFor(IS_VALID).name("Mandatory").expression("isValid != null")
                        .explanation("isValid must be set").build())
                .withConstraint(aRuleFor(IS_VALID, COMMENT).name("Comment_Needed_If_Not_Valid")
                        .expression("isValid || !isValid && comment != null")
                        .explanation("A comment is required when no validation").build())
                .build();
    }

}
