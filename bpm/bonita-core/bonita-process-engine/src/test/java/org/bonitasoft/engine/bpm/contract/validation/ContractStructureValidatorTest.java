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

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.*;
import static org.bonitasoft.engine.core.process.definition.model.SType.*;
import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        final SContractDefinition contract = aContract().withInput(aSimpleInput(TEXT).withName("aText")).build();
        final Map<String, Serializable> taskInputs = aMap()
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
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(TEXT).withName("aText"))
                .withInput(aSimpleInput(BOOLEAN).withName("aBoolean")).build();
        final Map<String, Serializable> taskInputs = aMap()
                .put("aText", "hello")
                .put("aBoolean", true).build();

        validator.validate(contract, taskInputs);

        // No exception expected
    }

    @Test
    public void should_pass_when_multiple_inputs_are_provided_and_valid() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(TEXT).withName("aText").withMultiple(true)).build();

        final List<String> values = new ArrayList<String>();
        values.add("hello");
        values.add("world");

        final Map<String, Serializable> taskInputs = aMap()
                .put("aText", (Serializable) values).build();

        validator.validate(contract, taskInputs);

        // No exception expected
    }

    @Test
    public void should_pass_when_multiple_complex_inputs_are_provided_and_valid() throws Exception {
        //given
        final SContractDefinition contract = aContract()
                .withInput(
                        aComplexInput().withName("complex").withMultiple(true)
                        .withInput(aSimpleInput(TEXT).withName("name").build(), aSimpleInput(SType.INTEGER).withName("value").build())).build();

        final List<Map<String, Serializable>> complexList = new ArrayList<Map<String, Serializable>>();
        complexList.add(aMap().put("name", "value1").put("value", 5).build());
        complexList.add(aMap().put("name", "value2").put("value", 8).build());

        final Map<String, Serializable> taskInputs = aMap()
                .put("complex", (Serializable) complexList).build();

        //then no exception expected
        try {
            validator.validate(contract, taskInputs);
        } catch (final ContractViolationException e) {
            fail(e.getExplanations().toString());
        }
    }

    @Test
    public void should_pass_when_complex_inputs_are_provided_and_valid() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(TEXT).withName("aText"))
                .withInput(aSimpleInput(BOOLEAN).withName("aBoolean"))
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();
        final Map<String, Serializable> taskInputs = aMap()
                .put("aText", "hello")
                .put("aBoolean", true)
                .put("complex", aMap().put("embedded", "aValue").build())
                .build();

        validator.validate(contract, taskInputs);
    }

    @Test
    public void should_throw_exception_with_explanations_when_inputs_are_missing() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(TEXT).withName("aText").build())
                .withInput(aSimpleInput(TEXT).withName("anotherText").build()).build();

        try {
            validator.validate(contract, new HashMap<String, Serializable>());
            fail("Expected ContractViolationException");
        } catch (final ContractViolationException e) {
            assertThat(e.getExplanations())
            .containsOnly("Expected input [aText] is missing", "Expected input [anotherText] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanations_when_complex_inputs_are_missing() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();

        try {
            validator.validate(contract, new HashMap<String, Serializable>());
            fail("Expected ContractViolationException");
        } catch (final ContractViolationException e) {
            assertThat(e.getExplanations())
            .containsOnly("Expected input [complex] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanations_when_complex_inputs_leaf_are_missing() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();
        final Map<String, Serializable> map = aMap().put("complex", aMap().build()).build();

        try {
            validator.validate(contract, map);
            fail("expected exception has not been thrown");
        } catch (final ContractViolationException e) {
            assertThat(e.getExplanations())
            .containsOnly("Expected input [embedded] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanation_when_types_are_not_valid() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(INTEGER).withName("anInteger"))
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();
        doThrow(new InputValidationException("type error explanation"))
        .when(typeValidator).validate(any(SInputDefinition.class), any(Object.class));
        final Map<String, Serializable> taskInputs = aMap().put("anInteger", "thisIsNotAnInteger").put("complex", "thisIsNotAComplex").build();

        try {
            validator.validate(contract, taskInputs);
            fail("expected exception has not been thrown");
        } catch (final ContractViolationException e) {
            assertThat(e.getExplanations())
            .containsOnly("type error explanation", "type error explanation");
        }
    }
}
