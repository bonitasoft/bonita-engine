/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
