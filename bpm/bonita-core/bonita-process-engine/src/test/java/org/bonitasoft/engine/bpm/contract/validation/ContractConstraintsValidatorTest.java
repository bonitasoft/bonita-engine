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
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.contractInputMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SConstraintDefinitionBuilder.aRuleFor;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractConstraintsValidatorTest {

    private static final String NICE_COMMENT = "no way!";
    private static final String COMMENT = "comment";
    private static final String IS_VALID = "isValid";

    @Mock
    private TechnicalLoggerService loggerService;

    private ContractConstraintsValidator validator;

    @Before
    public void setUp() {
        when(loggerService.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(true);
        when(loggerService.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(true);

        validator = new ContractConstraintsValidator(loggerService);
    }

    @Test
    public void should_log_all_rules_in_debug_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();

        validator.validate(contract, contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));

        //then
        verify(loggerService).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG, "Evaluating constraint [Mandatory] on input(s) [isValid]");
        verify(loggerService).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating constraint [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment]");
        verify(loggerService, never()).log(eq(ContractConstraintsValidator.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void should_not_log_all_rules_in_info_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();

        //given
        when(loggerService.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(false);
        when(loggerService.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(false);

        //
        validator.validate(contract, contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));

        //then
        verify(loggerService, never()).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating constraint [Mandatory] on input(s) [isValid]");
        verify(loggerService, never()).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating constraint [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment]");
        verify(loggerService, never()).log(eq(ContractConstraintsValidator.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void isValid_should_log_invalid_constraints_in_warning_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();

        try {
            validator.validate(contract, contractInputMap(entry(IS_VALID, false), entry(COMMENT, null)));
            fail("validation should fail");
        } catch (final ContractViolationException e) {
            verify(loggerService).log(ContractConstraintsValidator.class, TechnicalLogSeverity.WARNING,
                    "Constraint [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment] is not valid");
        }
    }

    @Test
    public void isValid_should_be_false_when_rule_fails_to_evaluate() throws Exception {
        //given
        final SContractDefinition contract = buildContractWithInputsAndConstraints();
        final SConstraintDefinitionImpl badRule = new SConstraintDefinitionImpl("bad rule", "a == b", "failing rule");
        contract.getConstraints().add(badRule);

        //when
        try {
            validator.validate(contract, contractInputMap(entry(IS_VALID, false), entry(COMMENT, NICE_COMMENT)));
            fail("validation should fail");
        } catch (final ContractViolationException e) {
            assertThat(e.getExplanations()).hasSize(1).containsExactly(badRule.getExplanation());
        }

    }

    private SContractDefinition buildContractWithInputsAndConstraints() {
        return aContract()
                .withInput(aSimpleInput(BOOLEAN).withName(IS_VALID).build())
                .withInput(aSimpleInput(BOOLEAN).withName(IS_VALID).build())
                .withConstraint(aRuleFor(IS_VALID).name("Mandatory").expression("isValid != null").explanation("isValid must be set").build())
                .withConstraint(aRuleFor(IS_VALID, COMMENT).name("Comment_Needed_If_Not_Valid").expression("isValid || !isValid && comment != null")
                        .explanation("A comment is required when no validation").build())
                .build();
    }
}
