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

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private ContractValidator validator;

    private SContractDefinition buildDefaultContract() {
        final SContractDefinitionImpl contract = new SContractDefinitionImpl();
        final SInputDefinitionImpl input1 = new SInputDefinitionImpl("isValid");
        input1.setType(Boolean.class.getName());
        contract.addInput(input1);
        final SInputDefinitionImpl input2 = new SInputDefinitionImpl("comment");
        input2.setType(String.class.getName());
        contract.addInput(input2);
        final SRuleDefinitionImpl rule1 = new SRuleDefinitionImpl("Mandatory", "isValid != null", "isValid must be set");
        rule1.addInputName("isValid");
        contract.addRule(rule1);
        final SRuleDefinitionImpl rule2 = new SRuleDefinitionImpl("Comment_Needed_If_Not_Valid", "isValid || !isValid && comment != null",
                "A comment is required when no validation");
        rule2.addInputName("isValid");
        rule2.addInputName("comment");
        contract.addRule(rule2);
        return contract;
    }

    @Test
    public void isValid_should_be_true_an_retrun_an_empty_list() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("isValid", true);
        variables.put("comment", null);
        final SContractDefinition contract = buildDefaultContract();

        final boolean valid = validator.isValid(contract, variables);
        assertThat(valid).isTrue();
        assertThat(validator.getComments()).isEmpty();
    }

    @Test
    public void isValid_should_be_false_an_retrun_explanations() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("isValid", false);
        variables.put("comment", null);
        final SContractDefinition contract = buildDefaultContract();

        final boolean valid = validator.isValid(contract, variables);
        assertThat(valid).isFalse();
        final List<String> explanations = validator.getComments();
        assertThat(explanations).hasSize(1);
        assertThat(explanations.get(0)).isEqualTo("A comment is required when no validation");
    }

    @Test
    public void isValid_should_log_all_rules_in_debug_mode() throws Exception {
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("isValid", false);
        variables.put("comment", "No Way!");
        final SContractDefinition contract = buildDefaultContract();
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
        variables.put("isValid", false);
        variables.put("comment", null);
        final SContractDefinition contract = buildDefaultContract();
        when(loggerService.isLoggable(ContractValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(false);
        when(loggerService.isLoggable(ContractValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(true);

        validator.isValid(contract, variables);

        verify(loggerService).log(ContractValidator.class, TechnicalLogSeverity.WARNING,
                "Rule [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment] is not valid");
        verify(loggerService, never()).log(eq(ContractValidator.class), eq(TechnicalLogSeverity.DEBUG), anyString());
    }

}
