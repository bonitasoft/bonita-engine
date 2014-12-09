/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.aBooleanField;
import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectValidationRuleTest {

    private BusinessObjectValidationRule businessObjectValidationRule;

    @Before
    public void setUp() {
        businessObjectValidationRule = new BusinessObjectValidationRule();
    }

    @Test
    public void should_apply_to_businessObject() {
        assertThat(businessObjectValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(businessObjectValidationRule.appliesTo(new SimpleField())).isFalse();
        assertThat(businessObjectValidationRule.appliesTo(new UniqueConstraint())).isFalse();

        assertThat(businessObjectValidationRule.appliesTo(new BusinessObject())).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouddCheckRule_throw_IllegalArgumentException() {
        businessObjectValidationRule.checkRule(new SimpleField());
    }

    @Test
    public void should_validate_that_qualified_name_is_not_null() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(null);

        final ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);

        assertThat(validationStatus).isNotOk();
    }
    
    @Test
    public void should_validate_that_qualified_name_is_not_starting_by_org_bonitasoft() throws Exception {
        final BusinessObject bo = aValidBusinesObject();
        bo.setQualifiedName("com.bonitasoft.Forbidden");

        final ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void shoudCheckRule_returns_valid_status() {
        final BusinessObject bo = aValidBusinesObject();
        
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isOk();

        bo.addUniqueConstraint("_UC_1", "firstName");
        validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isOk();
    }

    private BusinessObject aValidBusinesObject() {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo");
        
        SimpleField field = new SimpleField();
        field.setName("firstName");
        
        bo.addField(field);
        return bo;
    }

    @Test
    public void shoudCheckRule_returns_error_status() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();

        bo.setQualifiedName("org.bonita.Bo 2");
        final SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();

        bo.setQualifiedName("org.bonita.Bo2");
        bo.addUniqueConstraint("_UC_1", "dontExists");
        validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();

    }

    @Test
    public void shoudCheckRule_returns_error_status_for_duplicated_query_name() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        final SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        bo.addQuery("toto", "titi", List.class.getName());
        bo.addQuery("toto", "titi", List.class.getName());
        final ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void shoudCheckRule_returns_error_status_for_duplicated_constraint_name() {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        final SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        bo.addUniqueConstraint("toto", "firstName");
        bo.addUniqueConstraint("toto", "firstName");
        final ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_simple_name_contain_no_underscore() {
        final BusinessObject businessObject = aBO("Name_withUnderscore").withField(aBooleanField("field")).build();

        final ValidationStatus validationStatus = businessObjectValidationRule.validate(businessObject);

        assertThat(validationStatus).isNotOk();
    }
}
