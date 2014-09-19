/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class QueryValidationRuleTest {

    private QueryValidationRule queryValidationRule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        queryValidationRule = new QueryValidationRule();
    }

    @Test
    public void shouldAppliesTo_UniqueConstraint() {
        assertThat(queryValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(queryValidationRule.appliesTo(new BusinessObject())).isFalse();
        assertThat(queryValidationRule.appliesTo(new SimpleField())).isFalse();
        assertThat(queryValidationRule.appliesTo(new UniqueConstraint())).isFalse();
        assertThat(queryValidationRule.appliesTo(new Query())).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckRule_throw_IllegalArgumentException() {
        queryValidationRule.checkRule(new BusinessObject());
    }

    @Test
    public void should_validate_that_query_name_is_a_valid_java_identifier() {
        Query query = new Query();
        query.setName(" a not valid java identifier");

        ValidationStatus validationStatus = queryValidationRule.validate(query);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void shouldCheckRule_return_valid_status() {
        Query q = new Query("findByName", "Select toto where titi = toto", List.class.getName());
        ValidationStatus checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isTrue();
    }

    @Test
    public void shouldCheckRule_return_error_status_if_no_name() {
        Query q = new Query("", "Select toto where titi = toto", List.class.getName());
        ValidationStatus checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();

        q = new Query(null, "Select toto where titi = toto", List.class.getName());
        checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();
    }

    @Test
    public void shouldCheckRule_return_error_status_if_name_too_long() {
        Query q = new Query(
                "dsfhsdjkhfdjskfhjksdhfjksdhfjkshfjksdhfjksdhfjksdhfjksdhfjkdsfhjkdshfjjkdshfjskdhfjksdhfdjskhfhfjkhsdfkjsdhfksduzeiryzueiryuzieryuigsdhjgfhjgriuzegrjkg",
                "Select toto where titi = toto", List.class.getName());
        ValidationStatus checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();
    }

    @Test
    public void shouldCheckRule_return_error_status_if_no_content() {
        Query q = new Query("toto", "", List.class.getName());
        ValidationStatus checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();

        q = new Query("toto", null, List.class.getName());
        checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();
    }

    @Test
    public void shouldCheckRule_return_error_status_if_no_returnType() {
        Query q = new Query("toto", "select", "");
        ValidationStatus checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();

        q = new Query("toto", "select", "");
        checkRule = queryValidationRule.validate(q);
        assertThat(checkRule.isOk()).isFalse();
    }

}
