package org.bonitasoft.engine.bpm.contract.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.bpm.contract.validation.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.SInputDefinitionBuilder.anInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.bonitasoft.engine.core.process.definition.model.SType.INTEGER;
import static org.bonitasoft.engine.core.process.definition.model.SType.TEXT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractStructureValidatorTest {

    @Mock
    TechnicalLoggerService logger;

    @Mock
    ContractTypeValidator typeValidator;

    @InjectMocks
    private ContractStructureValidator validator;

    @Test
    public void should_pass_when_inputs_are_provided_and_valid() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(anInput(TEXT).withName("aText").build())
                .withInput(anInput(BOOLEAN).withName("aBoolean").build()).build();
        Map<String, Object> taskInputs = aMap()
                .put("aText", "hello")
                .put("aBoolean", true).build();

        validator.validate(contract.getSimpleInputs(), taskInputs);
    }

    @Test
    public void should_throw_exception_with_explanations_when_inputs_are_missing() throws Exception {
        try {
            SContractDefinition contract = aContract()
                    .withInput(anInput(TEXT).withName("aText").build())
                    .withInput(anInput(TEXT).withName("anotherText").build()).build();

            validator.validate(contract.getSimpleInputs(), new HashMap<String, Object>());
            fail("expected exception has not been thrown");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("Contract need field [aText] but it has not been provided", "Contract need field [anotherText] but it has not been provided");
        }
    }

    @Test
    public void should_throw_exception_with_explanation_when_types_are_not_valid() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(anInput(INTEGER).withName("anInteger").build())
                .withInput(anInput(BOOLEAN).withName("aBoolean").build()).build();
        doThrow(new InputValidationException("type error explanation"))
                .when(typeValidator).validate(any(SInputDefinition.class), any(Object.class));
        Map<String, Object> taskInputs = aMap().put("anInteger", "thisIsNotAnInteger").put("aBoolean", "thisIsNotABoolean").build();

        try {
            validator.validate(contract.getSimpleInputs(), taskInputs);
            fail("expected exception has not been thrown");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("type error explanation", "type error explanation");
        }
    }
}
