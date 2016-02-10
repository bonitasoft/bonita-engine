/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model;

import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.aStringField;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.anAggregationField;
import static com.bonitasoft.engine.bdm.model.assertion.BusinessObjectModelAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;

/**
 * @author Colin PUY
 */
public class BusinessObjectModelTest {

    @Test
    public void cannot_be_marshalled_when_it_has_no_business_object() {
        final BusinessObjectModel businessObjectModel = new BusinessObjectModel();

        assertThat(businessObjectModel).cannotBeMarshalled();
    }

    @Test
    public void can_be_marshalled_when_it_has_at_least_one_business_object() {
        final BusinessObjectModel businessObjectModel = new BusinessObjectModel();
        businessObjectModel.addBusinessObject(aBO("aBo").withField(aStringField("aField").build()).build());

        assertThat(businessObjectModel).canBeMarshalled();
    }

    @Test
    public void should_have_already_declared_business_object_in_relation_fields() {
        final BusinessObject notInModelBO = aBO("notInModelBo").withField(aStringField("aField").build()).build();
        final BusinessObjectModel businessObjectModel = new BusinessObjectModel();
        businessObjectModel.addBusinessObject(aBO("aBo").withField(anAggregationField("aggreg", notInModelBO)).build());

        assertThat(businessObjectModel).cannotBeMarshalled();

        // we add referenced BO to model
        businessObjectModel.addBusinessObject(notInModelBO);

        assertThat(businessObjectModel).canBeMarshalled();
    }

    @Test
    public void should_list_distincts_business_object_class_names() {
        final String aClassName = "aClassName";
        final String anotherClassName = "anotherClassName";
        final BusinessObjectModel bom = aBOM().withBOs(aBO(aClassName).build(), aBO(aClassName).build(), aBO(anotherClassName).build()).build();

        final Set<String> classNames = bom.getBusinessObjectsClassNames();

        assertThat(classNames).containsOnlyOnce(aClassName, anotherClassName);
    }

}
