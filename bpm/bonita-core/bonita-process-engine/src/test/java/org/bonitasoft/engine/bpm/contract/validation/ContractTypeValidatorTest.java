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

import static org.bonitasoft.engine.bpm.contract.validation.builder.SComplexInputDefinitionBuilder.aComplexInput;
import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.aSimpleInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;
import static org.bonitasoft.engine.core.process.definition.model.SType.DECIMAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;
import org.junit.Before;
import org.junit.Test;

public class ContractTypeValidatorTest {

    private ContractTypeValidator contractTypeValidator;

    @Before
    public void setUp() {
        contractTypeValidator = new ContractTypeValidator();
    }

    @Test(expected = InputValidationException.class)
    public void should_delegate_simple_type_validation_to_associated_enum() throws Exception {
        final SInputDefinition definition = aSimpleInput(BOOLEAN).build();

        contractTypeValidator.validate(definition, "not a boolean");
    }

    @Test
    public void should_decimal_validation_accept_integer() throws Exception {
        //given
        final SInputDefinition definition = aSimpleInput(DECIMAL).build();

        //when
        contractTypeValidator.validate(definition, 2);

        //then no exception
    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_null_for_a_complex_type() throws Exception {
        final SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");

        contractTypeValidator.validate(definition, null);
    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_non_map_object_for_complex_type() throws Exception {
        final SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");

        contractTypeValidator.validate(definition, "this is not a map");
    }

    @Test
    public void should_validate_map_object_for_complex_type() throws Exception {
        //given
        final SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition", "description", true,
                Arrays.asList((SSimpleInputDefinition) new SSimpleInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description")), null);

        definition.getSimpleInputDefinitions().add(
                new SSimpleInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description"));

        //when
        contractTypeValidator.validate(definition, new ArrayList<String>());

        // expected no exception
    }

    @Test
    public void should_validate_multiple_simple_type_empty_list() throws Exception {
        //given
        final SSimpleInputDefinitionImpl definition = new SSimpleInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, new ArrayList<String>());

        //then no exception

    }

    @Test
    public void should_validate_multiple_complex_type_empty_list() throws Exception {
        //given
        final SSimpleInputDefinitionImpl definition = new SSimpleInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, new ArrayList<String>());

        //then no exception

    }

    @Test
    public void should_validate_multiple_simple_type() throws Exception {
        //given
        final SSimpleInputDefinitionImpl definition = new SSimpleInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when
        contractTypeValidator.validate(definition, Arrays.asList("input1", "input2"));

        //then no exception

    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_multiple_simple_type_with_bad_values() throws Exception {
        //given
        final SSimpleInputDefinitionImpl definition = new SSimpleInputDefinitionImpl("a simple multiple definition", SType.DECIMAL, "description", true);

        //when then exception
        contractTypeValidator.validate(definition, Arrays.asList("not a number"));


    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_multiple_simple_type() throws Exception {
        //given
        final SSimpleInputDefinitionImpl definition = new SSimpleInputDefinitionImpl("a simple multiple definition", SType.TEXT, "description", true);

        //when then exception
        contractTypeValidator.validate(definition, "i am not a list");
    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_multiple_complex_type_when_no_list() throws Exception {
        //given

        final SSimpleInputDefinition simpleDefinition = new SSimpleInputDefinitionImpl("simpleInput", SType.TEXT, "description");
        final SComplexInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input").withMultiple(true)
                .withInput(simpleDefinition).build();

        //when then exception
        contractTypeValidator.validate(complexDefinition, "i am not a list");
    }

    @Test
    public void should_validate_multiple_complex_type() throws Exception {
        //given
        final SSimpleInputDefinition simpleDefinition = new SSimpleInputDefinitionImpl("simpleInput", SType.TEXT, "description");
        final SComplexInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input").withMultiple(true)
                .withInput(simpleDefinition).build();

        //when
        final Map<String, Object> complexInput = new HashMap<String, Object>();
        complexInput.put("simpleInput", "text value");
        final List<Map<String, Object>> complexList = new ArrayList<Map<String, Object>>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        //then no exception
        contractTypeValidator.validate(complexDefinition, complexList);

    }

    @Test
    public void should_validate_multiple_complex_with_multiple_complex_type() throws Exception {
        //given
        final SSimpleInputDefinition simpleDefinition = new SSimpleInputDefinitionImpl("simpleInput", SType.INTEGER, "description");
        final SComplexInputDefinition complexListDefinition = aComplexInput().withName("complexInComplex").withDescription("complex multiple input")
                .withMultiple(true)
                .withInput(simpleDefinition).build();
        final SComplexInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input")
                .withInput(complexListDefinition).build();

        //when
        final Map<String, Object> complexInput = new HashMap<String, Object>();
        complexInput.put("simpleInput", 123);
        final List<Map<String, Object>> complexList = new ArrayList<Map<String, Object>>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        final Map<String, Object> taskInput = new HashMap<String, Object>();
        taskInput.put("complexInComplex", complexList);

        final List<Map<String, Object>> taskInputList = new ArrayList<Map<String, Object>>();
        taskInputList.add(taskInput);
        taskInputList.add(taskInput);
        taskInputList.add(taskInput);

        //then no exception
        contractTypeValidator.validate(complexDefinition, taskInput);

    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_complex_with_multiple_complex_type() throws Exception {
        //given
        final SSimpleInputDefinition simpleDefinition = new SSimpleInputDefinitionImpl("simpleInput", SType.DATE, "description");
        final SComplexInputDefinition complexListDefinition = aComplexInput().withName("complexInComplex").withDescription("complex multiple input")
                .withMultiple(true)
                .withInput(simpleDefinition).build();
        final SComplexInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input")
                .withInput(complexListDefinition).withMultiple(true).build();

        //when
        final Map<String, Object> complexInput = new HashMap<String, Object>();
        complexInput.put("simpleInput", "not a date");
        final List<Map<String, Object>> complexList = new ArrayList<Map<String, Object>>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        final Map<String, Object> taskInput = new HashMap<String, Object>();
        taskInput.put("complexInComplex", complexList);

        //then no exception
        contractTypeValidator.validate(complexDefinition, taskInput);

    }

    @Test(expected = InputValidationException.class)
    public void should_not_validate_multiple_complex_type() throws Exception {
        //given
        final SSimpleInputDefinition simpleDefinition = new SSimpleInputDefinitionImpl("simpleInput", SType.INTEGER, "description");
        final SComplexInputDefinition complexDefinition = aComplexInput().withName("complexName").withDescription("complex multiple input").withMultiple(true)
                .withInput(simpleDefinition).build();

        //when
        final Map<String, Object> complexInput = new HashMap<String, Object>();

        complexInput.put("simpleInput", "zz");
        final List<Map<String, Object>> complexList = new ArrayList<Map<String, Object>>();
        complexList.add(complexInput);
        complexList.add(complexInput);
        complexList.add(complexInput);

        //then no exception
        contractTypeValidator.validate(complexDefinition, complexList);

    }

}
