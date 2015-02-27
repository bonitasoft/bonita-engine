/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Emmanuel Duchastenier
 */
public class QueryParameterValidationRuleTest {

    @Test
    public void checkRuleShouldForbidStartIndexAsQueryParameterName() {
        QueryParameter queryParam = new QueryParameter("startIndex", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors().get(0)).contains("is a reserved parameter name");
    }

    @Test
    public void checkRuleShouldForbidMaxResultsAsQueryParameterName() {
        QueryParameter queryParam = new QueryParameter("maxResults", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors()).hasSize(1);
        assertThat(status.getErrors().get(0)).contains("is a reserved parameter name");
    }

    @Test
    public void aQueryParameterNameShouldHaveAName() {
        QueryParameter queryParam = new QueryParameter();

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors().get(0)).contains("must have name");
    }

    @Test
    public void aQueryParameterNameShouldHaveANonEmptyName() {
        QueryParameter queryParam = new QueryParameter("", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors().get(0)).contains("must have name");
    }

    @Test
    public void aQueryParameterNameShouldHaveAVAlidJavaIdentifierName() {
        QueryParameter queryParam = new QueryParameter("1_manu", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors().get(0)).contains("is not a valid Java identifier");
    }

    @Test
    public void aQueryParameterClassNameShouldHaveAName() {
        QueryParameter queryParam = new QueryParameter("aValidParamName", null);

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors().get(0)).contains("query parameter must have a classname");
    }

    @Test
    public void aQueryParameterClassNameShouldHaveANonEmptyName() {
        QueryParameter queryParam = new QueryParameter("aValidParamName", "");

        ValidationStatus status = new QueryParameterValidationRule().validate(queryParam);

        assertThat(status.getErrors().get(0)).contains("query parameter must have a classname");
    }

}
