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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.aComplexInput;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.anInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.bonitasoft.engine.core.process.definition.model.SType.INTEGER;
import static org.bonitasoft.engine.core.process.definition.model.SType.TEXT;
import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.DEBUG;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractStructureValidatorTest {

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    ContractTypeValidator typeValidator;

    @InjectMocks
    private ContractStructureValidator validator;

    @Before
    public void setUp() {
        when(logger.isLoggable(ContractStructureValidator.class, DEBUG)).thenReturn(true);
    }
    
    @Test
    public void should_log_inputs_provided_but_not_in_defined_in_contract() throws Exception {
        SContractDefinition contract = aContract().withInput(anInput(TEXT).withName("aText")).build();
        Map<String, Object> taskInputs = aMap()
                .put("aText", "should be provided")
                .put("someFieldNotDefinedInContract", true)
                .put("someOtherFieldNotDefinedInContract", "42").build();

        validator.validate(contract, taskInputs);

        verify(logger).log(ContractStructureValidator.class, DEBUG,
                "Unexpected input [someFieldNotDefinedInContract] provided");
        verify(logger).log(ContractStructureValidator.class, DEBUG,
                "Unexpected input [someOtherFieldNotDefinedInContract] provided");
    }

    @Test
    public void should_pass_when_inputs_are_provided_and_valid() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(anInput(TEXT).withName("aText"))
                .withInput(anInput(BOOLEAN).withName("aBoolean")).build();
        Map<String, Object> taskInputs = aMap()
                .put("aText", "hello")
                .put("aBoolean", true).build();

        validator.validate(contract, taskInputs);
        
        // No exception expected
    }

    @Test
    public void should_pass_when_complex_inputs_are_provided_and_valid() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(anInput(TEXT).withName("aText"))
                .withInput(anInput(BOOLEAN).withName("aBoolean"))
                .withInput(aComplexInput().withName("complex").withInput(anInput(SType.TEXT).withName("embedded"))).build();
        Map<String, Object> taskInputs = aMap()
                .put("aText", "hello")
                .put("aBoolean", true)
                .put("complex", aMap().put("embedded", "aValue").build())
                .build();

        validator.validate(contract, taskInputs);
    }

    @Test
    public void should_throw_exception_with_explanations_when_inputs_are_missing() throws Exception {
            SContractDefinition contract = aContract()
                    .withInput(anInput(TEXT).withName("aText").build())
                    .withInput(anInput(TEXT).withName("anotherText").build()).build();

        try {
            validator.validate(contract, new HashMap<String, Object>());
            fail("Expected ContractViolationException");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("Expected input [aText] is missing", "Expected input [anotherText] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanations_when_complex_inputs_are_missing() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withInput(anInput(SType.TEXT).withName("embedded"))).build();

        try {
            validator.validate(contract, new HashMap<String, Object>());
            fail("Expected ContractViolationException");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("Expected input [complex] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanations_when_complex_inputs_leaf_are_missing() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withInput(anInput(SType.TEXT).withName("embedded"))).build();
        Map<String, Object> map = aMap().put("complex", aMap().build()).build();
        
        try {
            validator.validate(contract, map);
            fail("expected exception has not been thrown");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("Expected input [embedded] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanation_when_types_are_not_valid() throws Exception {
        SContractDefinition contract = aContract()
                .withInput(anInput(INTEGER).withName("anInteger"))
                .withInput(aComplexInput().withName("complex").withInput(anInput(SType.TEXT).withName("embedded"))).build();
        doThrow(new InputValidationException("type error explanation"))
                .when(typeValidator).validate(any(SInputDefinition.class), any(Object.class));
        Map<String, Object> taskInputs = aMap().put("anInteger", "thisIsNotAnInteger").put("complex", "thisIsNotAComplex").build();

        try {
            validator.validate(contract, taskInputs);
            fail("expected exception has not been thrown");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("type error explanation", "type error explanation");
        }
    }
}
