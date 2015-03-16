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

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aBooleanField;
import static org.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

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
        checkQualifiedNameValidationStatus("com.bonitasoft.Forbidden", false);

        checkQualifiedNameValidationStatus("com.bonitasoftextended", true);
        checkQualifiedNameValidationStatus("org.com.bonitasoft.model", true);
        checkQualifiedNameValidationStatus("org.bonitasoft.model", false);
        checkQualifiedNameValidationStatus("org.bonitasoftextended", true);
    }

    private void checkQualifiedNameValidationStatus(final String qualifiedName, final boolean expectedValidation) {
        // given
        final BusinessObject bo = aValidBusinesObject();
        bo.setQualifiedName(qualifiedName);

        // when
        final ValidationStatus validationStatus = businessObjectValidationRule.validate(bo);

        // then
        if (expectedValidation) {
            assertThat(validationStatus).isOk("should valid business object with qualified name:" + qualifiedName);
        } else {
            assertThat(validationStatus).isNotOk("should not valid business object with qualified name:" + qualifiedName);
        }
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
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Bo");

        final SimpleField field = new SimpleField();
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
