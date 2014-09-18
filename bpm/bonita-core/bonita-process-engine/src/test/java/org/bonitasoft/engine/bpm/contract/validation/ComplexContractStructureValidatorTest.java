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

import static org.bonitasoft.engine.bpm.contract.validation.SInputDefinitionBuilder.anInput;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComplexContractStructureValidatorTest {

    @Mock
    private ContractStructureValidator simpleInputValidator;
    
    @InjectMocks
    private ComplexContractStructureValidator validator;

    @Test
    public void should_validate_simple_inputs() throws Exception {
//        SContractDefinitionImpl contract = new SContractDefinitionImpl();
//        contract.addSimpleInput(anInput().build());
//        
//        validator.validate(contract, new HashMap<String, Object>());
//        
//        verify(simpleInputValidator).validate(contract, new HashMap<String, Object>());
    }
    
    @Test
    public void should_validate_complex_input_leaf() throws Exception {
        
    }
}
