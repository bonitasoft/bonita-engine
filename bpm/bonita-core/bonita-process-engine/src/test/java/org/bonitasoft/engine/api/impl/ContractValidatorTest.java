package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SRuleDefinitionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractValidatorTest {

    private static final String NICE_COMMENT = "no way!";

    private static final String COMMENT = "comment";

    private static final String IS_VALID = "isValid";

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private ContractValidator validator;

    private SContractDefinition buildContractWithInputsAndRules() {
        final SContractDefinitionImpl contract = buildEmptyContract();
        addInputsToContract(contract);
        addRulesToContractWithInputs(contract);
        return contract;
    }

    private void addRulesToContractWithInputs(final SContractDefinition contract) {
        final SRuleDefinitionImpl rule1 = new SRuleDefinitionImpl("Mandatory", "isValid != null", "isValid must be set");
        rule1.addInputName(IS_VALID);
        contract.getRules().add(rule1);
        final SRuleDefinitionImpl rule2 = new SRuleDefinitionImpl("Comment_Needed_If_Not_Valid", "isValid || !isValid && comment != null",
                "A comment is required when no validation");
        rule2.addInputName(IS_VALID);
        rule2.addInputName(COMMENT);
        contract.getRules().add(rule2);
    }

    private SContractDefinition addInputsToContract(final SContractDefinition contract) {
        final SInputDefinitionImpl input1 = new SInputDefinitionImpl(IS_VALID);
        input1.setType(Boolean.class.getName());
        contract.getInputs().add(input1);
        final SInputDefinitionImpl input2 = new SInputDefinitionImpl(COMMENT);
        input2.setType(String.class.getName());
        contract.getInputs().add(input2);
        return contract;
    }

    private SContractDefinitionImpl buildEmptyContract() {
        final SContractDefinitionImpl contract = new SContractDefinitionImpl();
        return contract;
    }

    @Test
    public void isValid_should_be_true_an_retrun_an_empty_list() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, true);
        variables.put(COMMENT, null);
        final SContractDefinition contract = buildContractWithInputsAndRules();

        final boolean valid = validator.isValid(contract, variables);
        assertThat(valid).isTrue();
        assertThat(validator.getComments()).isEmpty();
    }

    @Test
    public void isValid_should_be_false_an_retrun_explanations() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, null);
        final SContractDefinition contract = buildContractWithInputsAndRules();

        final boolean valid = validator.isValid(contract, variables);
        assertThat(valid).isFalse();
        final List<String> explanations = validator.getComments();
        assertThat(explanations).hasSize(1);
        assertThat(explanations.get(0)).isEqualTo("A comment is required when no validation");
    }

    @Test
    public void isValid_should_log_all_rules_in_debug_mode() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, NICE_COMMENT);
        final SContractDefinition contract = buildContractWithInputsAndRules();
        when(loggerService.isLoggable(ContractValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(true);
        when(loggerService.isLoggable(ContractValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(true);

        validator.isValid(contract, variables);

        verify(loggerService).log(ContractValidator.class, TechnicalLogSeverity.DEBUG, "Evaluating rule [Mandatory] on input(s) [isValid]");
        verify(loggerService).log(ContractValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating rule [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment]");
        verify(loggerService, never()).log(eq(ContractValidator.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void isValid_should_log_invalid_rules_in_warning_mode() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, null);
        final SContractDefinition contract = buildContractWithInputsAndRules();
        when(loggerService.isLoggable(ContractValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(false);
        when(loggerService.isLoggable(ContractValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(true);

        validator.isValid(contract, variables);

        verify(loggerService).log(ContractValidator.class, TechnicalLogSeverity.WARNING,
                "Rule [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment] is not valid");
        verify(loggerService, never()).log(eq(ContractValidator.class), eq(TechnicalLogSeverity.DEBUG), anyString());
    }

    @Test
    public void isValid_should_be_false_when_inputs_are_unexpected() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, null);
        final SContractDefinition contract = new SContractDefinitionImpl();
        contract.getInputs().add(new SInputDefinitionImpl(IS_VALID));

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("should refuse when inputs are unexpected").isFalse();
        assertThat(validator.getComments()).hasSize(1).containsExactly("variable " + COMMENT + " is not expected");
    }

    @Test
    public void isValid_should_be_false_when_inputs_are_missing() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(COMMENT, NICE_COMMENT);
        final SContractDefinition contract = new SContractDefinitionImpl();
        contract.getInputs().add(new SInputDefinitionImpl(IS_VALID));
        contract.getInputs().add(new SInputDefinitionImpl(COMMENT));

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("should refuse when inputs are unexpected").isFalse();
        assertThat(validator.getComments()).hasSize(1).containsExactly(IS_VALID + " is not defined");
    }

    @Test
    public void isValid_should_be_false_when_inputs_are_missing_and_contract_has_no_rules() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        final SContractDefinition contract = addInputsToContract(buildEmptyContract());

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("should refuse when inputs are unexpected").isFalse();
        assertThat(validator.getComments()).hasSize(1).containsExactly(COMMENT + " is not defined");
    }

    @Test
    public void isValid_should_be_true_when_no_contract_and_no_inputs() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        final SContractDefinition contract = buildEmptyContract();

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("shoul validate when contract is empty and no inputs are provided").isTrue();
    }

    @Test
    public void isValid_should_be_true_when_inputs_meets_contract() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, NICE_COMMENT);
        final SContractDefinition contract = buildContractWithInputsAndRules();

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("should validate contract").isTrue();
    }

    @Test
    public void isValid_should_be_true_when_inputs_meets_contract_without_rules() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, NICE_COMMENT);
        final SContractDefinition contract = addInputsToContract(buildEmptyContract());

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("should validate contract without rules").isTrue();
    }

    @Test
    public void isValid_should_be_false_when_rule_fails_to_evaluate() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, NICE_COMMENT);
        final SContractDefinition contract = buildContractWithInputsAndRules();
        final SRuleDefinitionImpl badRule = new SRuleDefinitionImpl("bad rule", "a == b", "failing rule");
        contract.getRules().add(badRule);

        //when
        final boolean valid = validator.isValid(contract, variables);

        //then
        assertThat(valid).as("should validate contract without rules").isFalse();
        assertThat(validator.getComments()).hasSize(1).containsExactly(badRule.getExplanation());
    }
}
