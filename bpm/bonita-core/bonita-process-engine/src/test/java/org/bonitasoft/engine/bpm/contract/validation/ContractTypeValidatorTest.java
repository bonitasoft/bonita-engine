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

import static org.bonitasoft.engine.bpm.contract.validation.builder.SSimpleInputDefinitionBuilder.anInput;
import static org.bonitasoft.engine.core.process.definition.model.SType.BOOLEAN;

import java.util.HashMap;

import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;
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
        SInputDefinition definition = anInput(BOOLEAN).build();
        
        contractTypeValidator.validate(definition, "not a boolean");
    }
    
    @Test(expected = InputValidationException.class)
    public void should_not_validate_null_for_a_complex_type() throws Exception {
        SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");
        
        contractTypeValidator.validate(definition, null);
    }
    
    @Test(expected = InputValidationException.class)
    public void should_not_validate_non_map_object_for_complex_type() throws Exception {
        SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");
        
        contractTypeValidator.validate(definition, "this is not a map");
    }
    
    @Test
    public void should_validate__map_object_for_complex_type() throws Exception {
        SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");
        
        contractTypeValidator.validate(definition, new HashMap<String, Object>());
        
        // expected no exception
    }
}
