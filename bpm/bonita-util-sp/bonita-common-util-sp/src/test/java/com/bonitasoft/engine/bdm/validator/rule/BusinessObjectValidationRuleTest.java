/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 */
package com.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
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

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        businessObjectValidationRule = new BusinessObjectValidationRule();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shoudAppliesTo_UniqueConstraint() throws Exception {
        assertThat(businessObjectValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(businessObjectValidationRule.appliesTo(new BusinessObject())).isTrue();
        assertThat(businessObjectValidationRule.appliesTo(new SimpleField())).isFalse();
        assertThat(businessObjectValidationRule.appliesTo(new UniqueConstraint())).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouddCheckRule_throw_IllegalArgumentException() throws Exception {
        businessObjectValidationRule.checkRule(new SimpleField());
    }

    @Test
    public void shoudCheckRule_returns_valid_status() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo");
        SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        ValidationStatus validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isTrue();

        bo.addUniqueConstraint("_UC_1", "firstName");
        validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isTrue();
    }

    @Test
    public void shoudCheckRule_returns_error_status() throws Exception {
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo2");
        ValidationStatus validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isFalse();

        bo.setQualifiedName("org.bonita.Bo 2");
        SimpleField field = new SimpleField();
        field.setName("firstName");
        bo.addField(field);
        validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isFalse();

        bo.setQualifiedName("org.bonita.Bo2");
        bo.addUniqueConstraint("_UC_1", "dontExists");
        validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isFalse();

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
        ValidationStatus validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isFalse();
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
        ValidationStatus validationStatus = businessObjectValidationRule.checkRule(bo);
        assertThat(validationStatus.isOk()).isFalse();
    }

}
