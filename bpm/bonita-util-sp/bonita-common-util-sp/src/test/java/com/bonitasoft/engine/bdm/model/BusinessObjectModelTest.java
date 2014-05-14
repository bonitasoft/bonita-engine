/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model;

import static com.bonitasoft.engine.bdm.model.assertion.BusinessObjectModelAssert.assertThat;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aStringField;

import org.junit.Test;

/**
 * @author Colin PUY
 */
public class BusinessObjectModelTest {

    @Test
    public void should_have_at_least_one_business_object() throws Exception {
        BusinessObjectModel businessObjectModel = new BusinessObjectModel();
        
        assertThat(businessObjectModel).cannotBeMarshalled();
        
        // 
        businessObjectModel.addBusinessObject(aBO("aBo").withField(aStringField("aField").build()).build());
        
        assertThat(businessObjectModel).canBeMarshalled();
    }

}
