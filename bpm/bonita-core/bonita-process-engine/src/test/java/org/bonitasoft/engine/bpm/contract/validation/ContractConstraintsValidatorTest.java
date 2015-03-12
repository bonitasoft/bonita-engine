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
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.aComplexInput;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SConstraintDefinitionBuilder.aRuleFor;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SConstraintType;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
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

    private static final String COMPLEX_INPUT_NAME = "complex";
    private static final String INTEGER_INPUT_NAME = "intInput";
    private static final String TEXT_INPUT_NAME = "textInput";
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

        validator = new ContractConstraintsValidator(loggerService, new ConstraintsDefinitionHelper(), new ContractVariableHelper());
    }

    @Test
    public void should_log_all_rules_in_debug_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();
        final Map<String, Object> variables = aMap().put(IS_VALID, false).put(COMMENT, NICE_COMMENT).build();

        validator.validate(contract, variables);

        //then
        verify(loggerService).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG, "Evaluating constraint [Mandatory] on input(s) [isValid]");
        verify(loggerService).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating constraint [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment]");
        verify(loggerService, never()).log(eq(ContractConstraintsValidator.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void should_not_log_all_rules_in_info_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndConstraints();
        final Map<String, Object> variables = aMap().put(IS_VALID, false).put(COMMENT, NICE_COMMENT).build();

        //given
        when(loggerService.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(false);
        when(loggerService.isLoggable(ContractConstraintsValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(false);

        //
        validator.validate(contract, variables);

        //then
        verify(loggerService, never()).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating constraint [Mandatory] on input(s) [isValid]");
        verify(loggerService, never()).log(ContractConstraintsValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating constraint [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment]");
        verify(loggerService, never()).log(eq(ContractConstraintsValidator.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void isValid_should_log_invalid_constraints_in_warning_mode() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, null);
        final SContractDefinition contract = buildContractWithInputsAndConstraints();

        try {
            validator.validate(contract, variables);
            fail("validation should fail");
        } catch (final ContractViolationException e) {
            verify(loggerService).log(ContractConstraintsValidator.class, TechnicalLogSeverity.WARNING,
                    "Constraint [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment] is not valid");
        }
    }

    @Test
    public void isValid_should_be_false_when_rule_fails_to_evaluate() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, NICE_COMMENT);
        final SContractDefinition contract = buildContractWithInputsAndConstraints();
        final SConstraintDefinitionImpl badRule = new SConstraintDefinitionImpl("bad rule", "a == b", "failing rule", SConstraintType.CUSTOM);
        contract.getConstraints().add(badRule);

        //when
        try {
            validator.validate(contract, variables);
            fail("validation should fail");
        } catch (final ContractViolationException e) {
            assertThat(e.getExplanations()).hasSize(1).containsExactly(badRule.getExplanation());
        }

    }

    @Test
    public void mandatory_rule_should_validate_input_in_complex_input() throws Exception {
        //given
        final SContractDefinition contractDefinition = aContract()
                .withMandatoryConstraint(TEXT_INPUT_NAME)
                .withMandatoryConstraint(INTEGER_INPUT_NAME)
                .withInput(
                        aComplexInput().withName(COMPLEX_INPUT_NAME).withInput(aSimpleInput(SType.TEXT).withName(TEXT_INPUT_NAME).build())
                                .withInput(aSimpleInput(SType.INTEGER).withName(INTEGER_INPUT_NAME)).build())
                .build();

        //when
        final Map<String, Object> variables = new HashMap<String, Object>();
        final Map<String, Object> complex = new HashMap<String, Object>();
        variables.put(TEXT_INPUT_NAME, null);
        variables.put(INTEGER_INPUT_NAME, null);
        complex.put(COMPLEX_INPUT_NAME, variables);

        //then
        try {
            validator.validate(contractDefinition, complex);
            fail("should not validate contract");
        } catch (final ContractViolationException e) {
            final List<String> explanations = e.getExplanations();
            assertThat(explanations).hasSize(2).contains("input " + TEXT_INPUT_NAME + " is mandatory")
                    .contains("input " + INTEGER_INPUT_NAME + " is mandatory");;
        }
    }

    @Test
    public void mandatory_rule_should_validate_multiple_simple_input() throws Exception {
        //given
        final SContractDefinition contractDefinition = aContract()
                .withMandatoryConstraint(TEXT_INPUT_NAME)
                .withInput(aSimpleInput(SType.TEXT).withName(TEXT_INPUT_NAME).withMultiple(true).build()).build();

        //when
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(TEXT_INPUT_NAME, Arrays.asList("valid input", "", null));

        //then
        try {
            validator.validate(contractDefinition, variables);
            fail("should not validate contract");
        } catch (final ContractViolationException e) {
            final List<String> explanations = e.getExplanations();
            assertThat(explanations).hasSize(2).contains("input " + TEXT_INPUT_NAME + " is mandatory");

        }
    }

    @Test
    public void mandatory_rule_should_validate_multiple_complex_input() throws Exception {
        //given
        final SContractDefinition contractDefinition = aContract()
                .withMandatoryConstraint(COMPLEX_INPUT_NAME)
                .withInput(
                        aComplexInput().withName(COMPLEX_INPUT_NAME).withMultiple(true)
                                .withInput(aSimpleInput(SType.INTEGER).withName(TEXT_INPUT_NAME).withMultiple(true).build()).build()).build();

        //when
        final Map<String, Object> goodComplex = new HashMap<String, Object>();
        final List<Map<String, Object>> complexList = new ArrayList<Map<String, Object>>();
        final Map<String, Object> variables = new HashMap<String, Object>();
        goodComplex.put(TEXT_INPUT_NAME, "aa");
        complexList.add(goodComplex);
        complexList.add(goodComplex);
        complexList.add(null);
        variables.put(COMPLEX_INPUT_NAME, complexList);
        //then
        try {
            validator.validate(contractDefinition, variables);
            fail("should not validate contract");
        } catch (final ContractViolationException e) {
            final List<String> explanations = e.getExplanations();
            assertThat(explanations).hasSize(1).contains("input " + COMPLEX_INPUT_NAME + " is mandatory");

        }
    }

    @Test
    public void should_validate_contract_rules_with_complex_and_mandatory() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(
                        aComplexInput().withName("user").withInput(aSimpleInput(SType.TEXT).withName("firstName").build())
                                .withInput(aSimpleInput(SType.TEXT).withName("lastName").build()))
                .withInput(
                        aComplexInput()
                                .withName("expenseReport")
                                .withInput(
                                        aComplexInput().withName("expenseLine").withMultiple(true)
                                                .withInput(aSimpleInput(SType.TEXT).withName("nature").build())
                                                .withInput(aSimpleInput(SType.DECIMAL).withName("amount").build())
                                                .withInput(aSimpleInput(SType.DATE).withName("date").build())
                                                .withInput(aSimpleInput(SType.TEXT).withName("comment").build()).build()).build())
                .withInput(aSimpleInput(SType.TEXT).build())
                .withMandatoryConstraint("firstName")
                .withMandatoryConstraint("lastName")
                .withMandatoryConstraint("nature")
                .withMandatoryConstraint("amount")
                .withMandatoryConstraint("date")
                .withMandatoryConstraint("comment")
                .build();

        //given
        final Map<String, Object> user = aMap().put("firstName", "john").put("lastName", "doe").build();
        final Map<String, Object> taxiExpenseLine = aMap().put("nature", "taxi").put("amount", 30).put("date", "2014-10-16").put("comment", "slow").build();
        final Map<String, Object> hotelExpenseLine = aMap().put("nature", "hotel").put("amount", 1000).put("date", "2014-10-16").put("comment", "expensive")
                .build();
        final List<Map<String, Object>> expenseLines = new ArrayList<Map<String, Object>>();
        expenseLines.add(taxiExpenseLine);
        expenseLines.add(hotelExpenseLine);
        final Map<String, Object> expenseReport = aMap().put("expenseReport", expenseLines).build();
        final Map<String, Object> variables = aMap().put("user", user).put("expenseReport", expenseReport).build();

        // when
        final ContractConstraintsValidator contractRulesValidator = new ContractConstraintsValidator(loggerService, new ConstraintsDefinitionHelper(),
                new ContractVariableHelper());

        //then
        try {
            contractRulesValidator.validate(contract, variables);
        } catch (final ContractViolationException e) {
            final List<String> explanations = e.getExplanations();
            assertThat(explanations).isEmpty();
        }

    }

    @Test
    public void should_not_validate_mandatory_constraint_with_several_input_names() throws Exception {
        final SConstraintDefinitionImpl badConstraint = new SConstraintDefinitionImpl("bad constraint", "input!=null", "should not validate",
                SConstraintType.MANDATORY);
        badConstraint.getInputNames().add("input");
        badConstraint.getInputNames().add("other input");

        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(SType.TEXT).withName("input").build())
                .withConstraint(badConstraint).build();

        //given
        final Map<String, Object> variables = aMap().put("input", "value").build();

        // when
        final ContractConstraintsValidator contractRulesValidator = new ContractConstraintsValidator(loggerService, new ConstraintsDefinitionHelper(),
                new ContractVariableHelper());

        //then
        try {
            contractRulesValidator.validate(contract, variables);
        } catch (final ContractViolationException e) {
            final List<String> explanations = e.getExplanations();
            assertThat(explanations).isNotNull().hasSize(1).containsExactly("Constraint [bad constraint] inputNames are not valid");
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
