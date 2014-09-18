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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.contract.validation.MapBuilder.aMap;
import static org.bonitasoft.engine.bpm.contract.validation.SInputDefinitionBuilder.anInput;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.mvel2.optimizers.impl.refl.nodes.ArrayLength;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@RunWith(MockitoJUnitRunner.class)
public class ComplexContractStructureValidatorTest {

    @Mock
    private ContractStructureValidator simpleInputValidator;
    
    @InjectMocks
    private ComplexContractStructureValidator validator;

    @Test
    public void should_validate_simple_inputs() throws Exception {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        contract.addSimpleInput(anInput().build());
        
        validator.validate(contract, new HashMap<String, Object>());
        
        verify(simpleInputValidator).validate(contract.getSimpleInputs(), new HashMap<String, Object>());
    }
    
    @Test
    public void should_validate_complex_input_leaf() throws Exception {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        List<SSimpleInputDefinition> asList = asList(anInput(SType.BOOLEAN).withName("aSimple").build());
        SComplexInputDefinitionImpl complex = new SComplexInputDefinitionImpl("complex", "acomplexInput", asList, new ArrayList<SComplexInputDefinition>());
        contract.addComplexInput(complex);
        
        Map<String,Object> simple = aMap().put("aSimple", true).build();
        Map<String,Object> put = aMap().put("complex", simple).build();
        
        validator.validate(contract, put);
        
        verify(simpleInputValidator).validate(asList, simple);
    }
    
    @Test
    public void should_tell_us_that_a_complex_type_is_missing() throws Exception {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        List<SSimpleInputDefinition> asList = asList(anInput(SType.BOOLEAN).withName("aSimple").build());
        SComplexInputDefinitionImpl complex = new SComplexInputDefinitionImpl("complex", "acomplexInput", asList, new ArrayList<SComplexInputDefinition>());
        contract.addComplexInput(complex);
        
        
        try {
            validator.validate(contract, new HashMap<String, Object>());
            fail("is missing");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations()).contains("Contract need field [complex] but it has not been provided");
        }
    }
    
    @Test
    public void should_check_complex_type() throws Exception {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        List<SSimpleInputDefinition> asList = asList(anInput(SType.BOOLEAN).withName("aSimple").build());
        SComplexInputDefinitionImpl complex = new SComplexInputDefinitionImpl("complex", "acomplexInput", asList, new ArrayList<SComplexInputDefinition>());
        contract.addComplexInput(complex);
        
        Map<String,Object> put = aMap().put("complex", "not a complex type").build();
        
        try {
            validator.validate(contract, put);
            fail("is missing");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations()).contains("not a complex type cannot be assigned to COMPLEX type");
        }
    }
    
    @Test
    public void complex_type_cannot_be_null() throws Exception {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        List<SSimpleInputDefinition> asList = asList(anInput(SType.BOOLEAN).withName("aSimple").build());
        SComplexInputDefinitionImpl complex = new SComplexInputDefinitionImpl("complex", "acomplexInput", asList, new ArrayList<SComplexInputDefinition>());
        contract.addComplexInput(complex);
        
        Map<String,Object> put = new HashMap<String, Object>();
        put.put("complex", null);
        
        try {
            validator.validate(contract, put);
            fail("is missing");
        } catch (ContractViolationException e) {
            assertThat(e.getExplanations()).contains("null cannot be assigned to COMPLEX type");
        }
    }
}
