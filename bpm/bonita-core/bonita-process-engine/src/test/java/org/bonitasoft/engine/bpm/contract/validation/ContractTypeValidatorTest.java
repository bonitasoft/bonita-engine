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
import static org.bonitasoft.engine.bpm.contract.validation.SInputDefinitionBuilder.anInput;
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
    
    @Test
    public void should_delegate_simple_type_validation_to_associated_enum() throws Exception {
        SInputDefinition definition = anInput(BOOLEAN).build();
        
        boolean validation = contractTypeValidator.isValid(definition, true);
        assertThat(validation).isTrue();
        
        validation = contractTypeValidator.isValid(definition, "not a boolean");
        assertThat(validation).isFalse();
    }
    
    @Test
    public void should_not_validate_null_for_a_complex_type() throws Exception {
        SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");
        
        boolean validation = contractTypeValidator.isValid(definition, null);
        
        assertThat(validation).isFalse();
    }
    
    @Test
    public void should_not_validate_non_map_object_for_complex_type() throws Exception {
        SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");
        
        boolean validation = contractTypeValidator.isValid(definition, "this is not a map");
        
        assertThat(validation).isFalse();
    }
    
    @Test
    public void should_validate__map_object_for_complex_type() throws Exception {
        SComplexInputDefinitionImpl definition = new SComplexInputDefinitionImpl("a complex definition");
        
        boolean validation = contractTypeValidator.isValid(definition, new HashMap<String, Object>());
        
        assertThat(validation).isTrue();
    }
    
    @Test
    public void should_not_validate_unknown_SInputDefinition_subclass() throws Exception {
        
        boolean validation = contractTypeValidator.isValid(new UnknownDefintion(), "a value");
        
        assertThat(validation).isFalse();
    }
    
    @SuppressWarnings("serial")
    private class UnknownDefintion implements SInputDefinition {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Long getId() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
        
    }
}
