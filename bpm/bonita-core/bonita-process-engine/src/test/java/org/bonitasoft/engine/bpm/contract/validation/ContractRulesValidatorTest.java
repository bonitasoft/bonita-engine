package org.bonitasoft.engine.bpm.contract.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.bpm.contract.validation.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.SInputDefinitionBuilder.anInput;
import static org.bonitasoft.engine.bpm.contract.validation.SRuleDefinitionBuilder.aRuleFor;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SRuleDefinitionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        return aContract()
                .withInput(anInput(BOOLEAN).withName(IS_VALID).build())
                .withInput(anInput(BOOLEAN).withName(IS_VALID).build())
                .withRule(aRuleFor(IS_VALID).name("Mandatory").expression("isValid != null").explanation("isValid must be set").build())
                .withRule(aRuleFor(IS_VALID, COMMENT).name("Comment_Needed_If_Not_Valid").expression("isValid || !isValid && comment != null")
                        .explanation("A comment is required when no validation").build())
                .build();
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
        final Map<String, Object> variables = new HashMap<String, Object>();
        variables.put(IS_VALID, false);
        variables.put(COMMENT, null);
        final SContractDefinition contract = buildContractWithInputsAndRules();

        try {
            validator.validate(contract.getRules(), variables);
            fail("validation should fail");
        } catch (Exception e) {
            verify(loggerService).log(ContractRulesValidator.class, TechnicalLogSeverity.WARNING,
                    "Rule [Comment_Needed_If_Not_Valid] on input(s) [isValid, comment] is not valid");
        }
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
        try {
            validator.validate(contract.getRules(), variables);
            fail("validation should fail");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations()).hasSize(1).containsExactly(badRule.getExplanation());
        }

    }

}
