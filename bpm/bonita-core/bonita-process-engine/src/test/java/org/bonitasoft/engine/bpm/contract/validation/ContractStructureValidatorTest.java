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
 */
package org.bonitasoft.engine.bpm.contract.validation;

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.MapBuilder.contractInputMap;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.aComplexInput;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SContractDefinitionBuilder.aContract;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.*;
import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.DEBUG;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractStructureValidatorTest {

    @Mock
    ContractTypeValidator typeValidator;
    @Mock
    private TechnicalLoggerService logger;
    @InjectMocks
    private ContractStructureValidator validator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        when(logger.isLoggable(ContractStructureValidator.class, DEBUG)).thenReturn(true);
        doReturn(true).when(typeValidator).validate(any(SInputDefinition.class), any(), any(ErrorReporter.class));
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

        final List<String> values = new ArrayList<>();
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
                                .withInput(aSimpleInput(TEXT).withName("name").build(), aSimpleInput(SType.INTEGER).withName("value").build()))
                .build();

        final List<Map<String, Serializable>> complexList = new ArrayList<>();
        complexList.add(aMap().put("name", "value1").put("value", 5).build());
        complexList.add(aMap().put("name", "value2").put("value", 8).build());

        final Map<String, Serializable> taskInputs = aMap()
                .put("complex", (Serializable) complexList).build();

        //then no exception expected
        try {
            validator.validate(contract, taskInputs);
        } catch (final SContractViolationException e) {
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
            fail("Expected SContractViolationException");
        } catch (final SContractViolationException e) {
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
            fail("Expected SContractViolationException");
        } catch (final SContractViolationException e) {
            assertThat(e.getExplanations())
                    .containsOnly("Expected input [complex] is missing");
        }
    }

    @Test
    public void should_throw_exception_with_explanations_when_complex_inputs_leaves_are_missing() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();
        final Map<String, Serializable> map = aMap().put("complex", aMap().build()).build();
        try {
            validator.validate(contract, map);
            fail("expected exception has not been thrown");
        } catch (final SContractViolationException e) {
            assertThat(e.getExplanations()).containsOnly("Expected input [embedded] is missing");
        }
    }

    @Test
    public void should_not_check_children_when_complex_input_is_null() throws Exception {
        // Given
        final SInputDefinition complex = aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded")).build();
        final SContractDefinition contract = aContract().withInput(complex).build();
        final Map<String, Serializable> inputs = Collections.singletonMap("complex", null);
        final ContractStructureValidator spy = spy(this.validator);
        // when
        spy.validate(contract, inputs);
        // then
        verify(spy, times(0)).validateInputContainer(eq(complex), eq(inputs), any(ErrorReporter.class));
    }

    @Test
    public void should_not_check_type_validation_when_input_is_null() throws Exception {
        final SContractDefinition contract = aContract().withInput(aSimpleInput(TEXT).withName("someNullText")).build();
        validator.validate(contract, Collections.<String, Serializable> singletonMap("someNullText", null));
        verifyZeroInteractions(typeValidator);
    }

    @Test
    public void should_call_type_validator_when_types_are_not_valid() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(INTEGER).withName("anInteger"))
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();
        doReturn(false).when(typeValidator).validate(any(SInputDefinition.class), any(Object.class), any(ErrorReporter.class));
        final Map<String, Serializable> taskInputs = aMap().put("anInteger", "thisIsNotAnInteger").put("complex", "thisIsNotAComplex").build();

        validator.validate(contract, taskInputs);

        verify(typeValidator).validate(any(SInputDefinition.class), eq("thisIsNotAComplex"), any(ErrorReporter.class));
    }

    @Test
    public void validate_should_handle_null_inputs_map_argument_as_empty_map() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aSimpleInput(INTEGER).withName("anInteger"))
                .withInput(aComplexInput().withName("complex").withInput(aSimpleInput(SType.TEXT).withName("embedded"))).build();
        final ErrorReporter errorReporter = new ErrorReporter();
        errorReporter.addError("plop");
        try {
            validator.validate(contract, null);
            fail("expected exception has not been thrown");
        } catch (final SContractViolationException e) {
            assertThat(e.getExplanations()).containsOnly("Expected input [anInteger] is missing", "Expected input [complex] is missing");
        }
    }

    @Test
    public void should_not_throw_exception_for_present_simple_input_with_null_value() throws Exception {
        final SContractDefinition contract = aContract().withInput(aSimpleInput(TEXT).withName("firstName")).build();

        validator.validate(contract, contractInputMap(entry("firstName", null)));
    }

    @Test
    public void should_not_throw_exception_for_present_complex_input_with_null_value() throws Exception {
        final SContractDefinition contract = aContract().withInput(aComplexInput().withName("employee")).build();

        validator.validate(contract, contractInputMap(entry("employee", null)));
    }

    @Test
    public void should_not_throw_exception_for_present_multiple_complex_input_with_empty_list() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withMultiple(true).withInput(aSimpleInput(TEXT).withName("name").build())).build();

        final Map<String, Serializable> taskInputs = aMap().put("complex", (Serializable) Collections.emptyList()).build();

        validator.validate(contract, taskInputs);
    }

    @Test
    public void should_ignore_null_values_for_multiple_complex_input() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withMultiple(true).withInput(
                        aSimpleInput(TEXT).withName("plic").build()))
                .build();

        final List<Map<String, Serializable>> complexList = new ArrayList<>();
        complexList.add(null);

        final Map<String, Serializable> taskInputs = aMap().put("complex", (Serializable) complexList).build();

        validator.validate(contract, taskInputs);
    }

    @Test
    public void should_ignore_null_values_for_multiple_complex_input_and_validate_next_element() throws Exception {
        final SContractDefinition contract = aContract()
                .withInput(aComplexInput().withName("complex").withMultiple(true).withInput(
                        aSimpleInput(TEXT).withName("plic").build(),
                        aSimpleInput(INTEGER).withName("plac").build()))
                .build();

        final List<Map<String, Serializable>> complexList = new ArrayList<>();
        complexList.add(null);
        complexList.add(aMap().put("plic", "value2").build());

        final Map<String, Serializable> taskInputs = aMap().put("complex", (Serializable) complexList).build();

        try {
            validator.validate(contract, taskInputs);
            fail("Should throw validation exception");
        } catch (final SContractViolationException e) {
            assertThat(e.getExplanations()).containsOnly("Expected input [plac] is missing");
        }
    }

}
