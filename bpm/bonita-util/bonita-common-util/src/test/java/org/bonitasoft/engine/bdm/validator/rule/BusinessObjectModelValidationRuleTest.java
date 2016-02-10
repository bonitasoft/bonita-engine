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

import static org.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.RelationField.FetchType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidationRuleTest {

    private BusinessObjectModelValidationRule businessObjectModelValidationRule;

    @Before
    public void setUp() {
        businessObjectModelValidationRule = new BusinessObjectModelValidationRule();
    }

    @Test
    public void should_apply_to_businessObjectModel() {
        assertThat(businessObjectModelValidationRule.appliesTo(new BusinessObject())).isFalse();
        assertThat(businessObjectModelValidationRule.appliesTo(new SimpleField())).isFalse();
        assertThat(businessObjectModelValidationRule.appliesTo(new UniqueConstraint())).isFalse();

        assertThat(businessObjectModelValidationRule.appliesTo(new BusinessObjectModel())).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouddCheckRule_throw_IllegalArgumentException() {
        businessObjectModelValidationRule.checkRule(new SimpleField());
    }

    @Test
    public void should_validate_that_bom_has_at_least_one_businessObject() {
        final BusinessObjectModel bom = new BusinessObjectModel();

        final ValidationStatus validationStatus = businessObjectModelValidationRule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_return_a_valid_status_when_bom_is_valid() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(new BusinessObject());

        final ValidationStatus validationStatus = businessObjectModelValidationRule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_return_a_error_status_when_bom_contains_invalid_query_names() {
        final BusinessObjectModel bom = new BusinessObjectModel();

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("Employee");
        final BusinessObject address = new BusinessObject();
        address.setQualifiedName("Address");

        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);
        address.addField(street);

        final RelationField addresses = new RelationField();
        addresses.setName("addresses");
        addresses.setCollection(true);
        addresses.setFetchType(FetchType.LAZY);
        addresses.setReference(address);

        employee.addField(addresses);

        address.addQuery("findAddressesByEmployeePersistenceId", "", List.class.getName());//Duplicated query name

        bom.addBusinessObject(employee);
        bom.addBusinessObject(address);

        final ValidationStatus validationStatus = businessObjectModelValidationRule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }
}
