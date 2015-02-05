/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.model;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aBooleanField;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.anAggregationField;
import static org.bonitasoft.engine.bdm.model.assertion.BusinessObjectAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

/**
 * @author Colin PUY
 */
public class BusinessObjectTest {

    @Test
    public void should_have_a_qualifiedName_and_at_least_one_field() {
        BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName("aQualifiedName");

        assertThat(businessObject).cannotBeMarshalled();

        //
        businessObject = new BusinessObject();
        businessObject.addField(aBooleanField("aField"));

        assertThat(businessObject).cannotBeMarshalled();

        //
        businessObject = new BusinessObject();
        businessObject.setQualifiedName("aQualifiedName");
        businessObject.addField(aBooleanField("aField"));

        assertThat(businessObject).canBeMarshalled();
    }

    @Test
    public void could_have_simpleFields_and_relationFields() {
        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName("aQualifiedName");
        businessObject.addField(aBooleanField("aSimpleField"));
        businessObject.addField(anAggregationField("aggregationField", aBO("boName").withField(aBooleanField("aField")).build()));

        assertThat(businessObject).canBeMarshalled();
    }

    @Test
    public void could_have_relationFields_referencing_itself() {
        final BusinessObject bo = aBO("aBo").build();

        bo.addField(anAggregationField("itselfRef", bo));

        assertThat(bo).canBeMarshalled();
    }

    @Test
    public void could_have_optional_uniqueConstraints() {
        final BusinessObject bo = aBO("aBo").withField(aBooleanField("field1")).withField(aBooleanField("field2")).build();

        bo.addUniqueConstraint("const", "field1");
        bo.addUniqueConstraint("const2", "field2");

        assertThat(bo).canBeMarshalled();
    }

    @Test
    public void could_have_optional_queries() {
        final BusinessObject bo = aBO("aBo").withField(aBooleanField("field")).build();

        bo.addQuery("query", "select something from something", "returnType");

        assertThat(bo).canBeMarshalled();
    }

    @Test
    public void should_addQuery() {
        final BusinessObject businessObject = new BusinessObject();

        final Query query = businessObject.addQuery("userByName", "SELECT u FROM User u WHERE u.name='romain'", List.class.getName());

        assertThat(businessObject.getQueries()).containsExactly(query);
    }

}
