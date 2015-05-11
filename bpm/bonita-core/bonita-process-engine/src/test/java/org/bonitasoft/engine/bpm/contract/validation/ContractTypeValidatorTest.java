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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.aComplexInput;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.bonitasoft.engine.core.process.definition.model.SType.DECIMAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;
import org.junit.Before;
import org.junit.Test;

public class ContractTypeValidatorTest {

    private ContractTypeValidator contractTypeValidator;
    private ErrorReporter errorReporter;

    @Before
    public void setUp() {
        contractTypeValidator = new ContractTypeValidator();
        errorReporter = new ErrorReporter();
    }

    @Test
    public void should_delegate_simple_type_validation_to_associated_enum() throws Exception {
        final SInputDefinition definition = aSimpleInput(BOOLEAN).build();

        contractTypeValidator.validate(definition, "not a boolean", errorReporter);

        // then
        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("not a boolean cannot be assigned to BOOLEAN");
    }

    @Test
    public void should_decimal_validation_accept_integer() throws Exception {
        //given
        final SInputDefinition definition = aSimpleInput(DECIMAL).build();

        //when
        contractTypeValidator.validate(definition, 2, errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();
    }

    @Test
    public void should_not_validate_null_for_a_complex_type() throws Exception {
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a complex definition","");

        contractTypeValidator.validate(definition, null, errorReporter);

        assertThat(errorReporter.hasError()).isTrue();
    }

    @Test
    public void should_not_validate_non_map_object_for_complex_type() throws Exception {
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a complex definition","");

        contractTypeValidator.validate(definition, "this is not a map", errorReporter);

        assertThat(errorReporter.hasError()).isTrue();
    }

    @Test
    public void should_validate_map_object_for_complex_type() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a complex definition", "description", true,
                Arrays.<SInputDefinition>asList(new SInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description")));

        definition.getInputDefinitions().add(
                new SInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description"));

        //when
        contractTypeValidator.validate(definition, new ArrayList<String>(), errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();
    }

    @Test
    public void should_validate_multiple_simple_type_empty_list() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, new ArrayList<String>(), errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();

    }

    @Test
    public void should_validate_multiple_complex_type_empty_list() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, new ArrayList<String>(), errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();

    }

    @Test
    public void should_validate_multiple_simple_type() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, Arrays.asList("input1", "input2"), errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();

    }

    @Test
    public void should_validate_file_input() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("theFile", SType.FILE, "description", false, Arrays.<SInputDefinition>asList(new SInputDefinitionImpl("","")));

        //when
        contractTypeValidator.validate(definition, new FileInputValue("theFile",new byte[]{0,1}), errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();

    }

    @Test
    public void should_not_validate_multiple_simple_type_with_bad_values() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a simple multiple definition", SType.DECIMAL, "description", true);

        //when
        contractTypeValidator.validate(definition, Collections.singletonList("not a number"), errorReporter);

        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("[not a number] cannot be assigned to DECIMAL");
    }

    @Test
    public void should_not_validate_multiple_simple_type() throws Exception {
        //given
        final SInputDefinitionImpl definition = new SInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, "i am not a list", errorReporter);

        // then
        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("i am not a list cannot be assigned to TEXT");
    }

    @Test
    public void should_not_validate_multiple_complex_type_when_no_list() throws Exception {
        //given

        final SInputDefinition simpleDefinition = new SInputDefinitionImpl("simpleInput", SType.TEXT, "description");
        final SInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input").withMultiple(true)
                .withInput(simpleDefinition).build();

        //when
        contractTypeValidator.validate(complexDefinition, "i am not a list", errorReporter);

        // then
        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("i am not a list cannot be assigned to COMPLEX type");
    }

    @Test
    public void should_validate_multiple_complex_type() throws Exception {
        //given
        final SInputDefinition simpleDefinition = new SInputDefinitionImpl("simpleInput", SType.TEXT, "description");
        final SInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input").withMultiple(true)
                .withInput(simpleDefinition).build();

        //when
        final Map<String, Object> complexInput = new HashMap<>();
        complexInput.put("simpleInput", "text value");
        final List<Map<String, Object>> complexList = new ArrayList<>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        //then no exception
        contractTypeValidator.validate(complexDefinition, complexList, errorReporter);
    }

    @Test
    public void should_validate_multiple_complex_with_multiple_complex_type() throws Exception {
        //given
        final SInputDefinition simpleDefinition = new SInputDefinitionImpl("simpleInput", SType.INTEGER, "description");
        final SInputDefinition complexListDefinition = aComplexInput().withName("complexInComplex").withDescription("complex multiple input")
                .withMultiple(true)
                .withInput(simpleDefinition).build();
        final SInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input")
                .withInput(complexListDefinition).build();

        //when
        final Map<String, Object> complexInput = new HashMap<>();
        complexInput.put("simpleInput", 123);
        final List<Map<String, Object>> complexList = new ArrayList<>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        final Map<String, Object> taskInput = new HashMap<>();
        taskInput.put("complexInComplex", complexList);

        final List<Map<String, Object>> taskInputList = new ArrayList<>();
        taskInputList.add(taskInput);
        taskInputList.add(taskInput);
        taskInputList.add(taskInput);
        contractTypeValidator.validate(complexDefinition, taskInput, errorReporter);

        // then
        assertThat(errorReporter.hasError()).isFalse();

    }

    @Test
    public void should_not_validate_complex_with_multiple_complex_type() throws Exception {
        //given
        final SInputDefinition simpleDefinition = new SInputDefinitionImpl("simpleInput", SType.DATE, "description");
        final SInputDefinition complexListDefinition = aComplexInput().withName("complexInComplex").withDescription("complex multiple input")
                .withMultiple(true)
                .withInput(simpleDefinition).build();
        final SInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input")
                .withInput(complexListDefinition).withMultiple(true).build();

        //when
        final Map<String, Object> complexInput = new HashMap<>();
        complexInput.put("simpleInput", "not a date");
        final List<Map<String, Object>> complexList = new ArrayList<>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        final Map<String, Object> taskInput = new HashMap<>();
        taskInput.put("complexInComplex", complexList);

        contractTypeValidator.validate(complexDefinition, taskInput, errorReporter);

        // then
        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("{complexInComplex=[{simpleInput=not a date}, {simpleInput=not a date}, {simpleInput=not a date}]} cannot be assigned to COMPLEX type");

    }

    @Test
    public void should_not_validate_complex_that_has_not_a_map_as_value() throws Exception {
        //given
        final SInputDefinition simpleDefinition = new SInputDefinitionImpl("simpleInput", SType.TEXT, "description");
        final SInputDefinition complexDefinition = aComplexInput().withName("complexInComplex").withDescription("complex multiple input")
                .withInput(simpleDefinition).build();

        //when
        contractTypeValidator.validate(complexDefinition, "not a complex", errorReporter);

        // then
        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("not a complex cannot be assigned to COMPLEX type");

    }

    @Test
    public void should_not_validate_multiple_complex_type() throws Exception {
        //given
        final SInputDefinition simpleDefinition = new SInputDefinitionImpl("simpleInput", SType.INTEGER, "description");
        final SInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input").withMultiple(true)
                .withInput(simpleDefinition).build();

        //when
        final List<Map<String, Object>> complexList = new ArrayList<>();
        complexList.add(Collections.<String, Object>singletonMap("simpleInput","aa"));
        complexList.add(Collections.<String, Object>singletonMap("simpleInput","bb"));
        complexList.add(Collections.<String, Object>singletonMap("simpleInput","cc"));

        contractTypeValidator.validate(complexDefinition, complexList, errorReporter);

        // then
        assertThat(errorReporter.hasError()).isTrue();
        assertThat(errorReporter.getErrors()).containsExactly("aa cannot be assigned to INTEGER", "bb cannot be assigned to INTEGER", "cc cannot be assigned to INTEGER");

    }

}
