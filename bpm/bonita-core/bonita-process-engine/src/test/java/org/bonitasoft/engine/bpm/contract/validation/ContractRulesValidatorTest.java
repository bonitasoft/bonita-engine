package org.bonitasoft.engine.bpm.contract.validation;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SRuleDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@RunWith(MockitoJUnitRunner.class)
public class ContractRulesValidatorTest {

    private static final String NICE_COMMENT = "no way!";
    private static final String COMMENT = "comment";
    private static final String IS_VALID = "isValid";

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private ContractRulesValidator validator;

    @Before
    public void setUp() {
        when(loggerService.isLoggable(ContractRulesValidator.class, TechnicalLogSeverity.DEBUG)).thenReturn(true);
        when(loggerService.isLoggable(ContractRulesValidator.class, TechnicalLogSeverity.WARNING)).thenReturn(true);
    }

    private SContractDefinition buildContractWithInputsAndRules() {
        final SContractDefinitionImpl contract = buildEmptyContract();
        addInputsToContract(contract);
        addRulesToContractWithInputs(contract);
        return contract;
    }

    private SContractDefinitionImpl buildEmptyContract() {
        return new SContractDefinitionImpl();
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
        final SSimpleInputDefinitionImpl input1 = new SSimpleInputDefinitionImpl(IS_VALID);
        input1.setType(SType.BOOLEAN);
        contract.getSimpleInputs().add(input1);
        final SSimpleInputDefinitionImpl input2 = new SSimpleInputDefinitionImpl(COMMENT);
        input2.setType(SType.TEXT);
        contract.getSimpleInputs().add(input2);
        return contract;
    }

    private Builder<String, Object> aMap() {
        return ImmutableMap.<String, Object> builder();
    }

    @Test
    public void should_log_all_rules_in_debug_mode() throws Exception {
        final SContractDefinition contract = buildContractWithInputsAndRules();
        Map<String, Object> variables = aMap().put(IS_VALID, false).put(COMMENT, NICE_COMMENT).build();
        
        validator.validate(contract.getRules(), variables);

        //then
        verify(loggerService).log(ContractRulesValidator.class, TechnicalLogSeverity.DEBUG, "Evaluating rule [Mandatory] on input(s) [isValid]");
        verify(loggerService).log(ContractRulesValidator.class, TechnicalLogSeverity.DEBUG,
                "Evaluating rule [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment]");
        verify(loggerService, never()).log(eq(ContractRulesValidator.class), eq(TechnicalLogSeverity.WARNING), anyString());
    }

    @Test
    public void isValid_should_log_invalid_rules_in_warning_mode() throws Exception {
        //given
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, null);
        final SContractDefinition contract = buildContractWithInputsAndRules();

        //when
        try {
            validator.validate(contract.getRules(), variables);
        } catch (Exception e) {
        }

        //then
        verify(loggerService).log(ContractRulesValidator.class, TechnicalLogSeverity.WARNING,
                "Rule [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment] is not valid");
    }
}
