/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 */
package com.bonitasoft.engine.bdm.validator.rule;

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
 * 
 */
public class BusinessObjectValidationRuleTest {

    private BusinessObjectValidationRule businessObjectValidationRule;

    @Before
    public void setUp() throws Exception {
        businessObjectValidationRule = new BusinessObjectValidationRule();
    }

    @Test
    public void should_apply_to_businessObject() throws Exception {
        assertThat(businessObjectValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(businessObjectValidationRule.appliesTo(new SimpleField())).isFalse();
        assertThat(businessObjectValidationRule.appliesTo(new UniqueConstraint())).isFalse();

        assertThat(businessObjectValidationRule.appliesTo(new BusinessObject())).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouddCheckRule_throw_IllegalArgumentException() throws Exception {
        businessObjectValidationRule.checkRule(new SimpleField());
    }

    @Test
    public void should_validate_that_qualified_name_is_not_null() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(null);
        
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        
        assertThat(validationStatus).isNotOk();
    }
    
    @Test
    public void shoudCheckRule_returns_valid_status() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo");
        SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isOk();

        bo.addUniqueConstraint("_UC_1", "firstName");
        validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isOk();
    }

    @Test
    public void shoudCheckRule_returns_error_status() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();

        bo.setQualifiedName("org.bonita.Bo 2");
        SimpleField field = new SimpleField();
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
    public void shoudCheckRule_returns_error_status_for_duplicated_query_name() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        bo.addQuery("toto", "titi", List.class.getName());
        bo.addQuery("toto", "titi", List.class.getName());
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void shoudCheckRule_returns_error_status_for_duplicated_constraint_name() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        bo.addUniqueConstraint("toto", "firstName");
        bo.addUniqueConstraint("toto", "firstName");
        ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);
        assertThat(validationStatus).isNotOk();
    }

}
