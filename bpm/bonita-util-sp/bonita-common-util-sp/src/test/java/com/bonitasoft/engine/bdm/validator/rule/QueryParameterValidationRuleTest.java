package com.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.bonitasoft.engine.bdm.model.QueryParameter;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Emmanuel Duchastenier
 */
public class QueryParameterValidationRuleTest {

    @Test
    public void checkRuleShouldForbidStartIndexAsQueryParameterName() throws Exception {
        QueryParameter queryParam = new QueryParameter("startIndex", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors().get(0)).contains("is a reserved parameter name");
    }

    @Test
    public void checkRuleShouldForbidMaxResultsAsQueryParameterName() throws Exception {
        QueryParameter queryParam = new QueryParameter("maxResults", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors()).hasSize(1);
        assertThat(status.getErrors().get(0)).contains("is a reserved parameter name");
    }

    @Test
    public void aQueryParameterNameShouldHaveAName() throws Exception {
        QueryParameter queryParam = new QueryParameter();

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors().get(0)).contains("must have name");
    }

    @Test
    public void aQueryParameterNameShouldHaveANonEmptyName() throws Exception {
        QueryParameter queryParam = new QueryParameter("", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors().get(0)).contains("must have name");
    }

    @Test
    public void aQueryParameterNameShouldHaveAVAlidJavaIdentifierName() throws Exception {
        QueryParameter queryParam = new QueryParameter("1_manu", Object.class.getName());

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors().get(0)).contains("is not a valid Java identifier");
    }

    @Test
    public void aQueryParameterClassNameShouldHaveAName() throws Exception {
        QueryParameter queryParam = new QueryParameter("aValidParamName", null);

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors().get(0)).contains("query parameter must have a classname");
    }

    @Test
    public void aQueryParameterClassNameShouldHaveANonEmptyName() throws Exception {
        QueryParameter queryParam = new QueryParameter("aValidParamName", "");

        ValidationStatus status = new QueryParameterValidationRule().checkRule(queryParam);

        assertThat(status.getErrors().get(0)).contains("query parameter must have a classname");
    }

}
