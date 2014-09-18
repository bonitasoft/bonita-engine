/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
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
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.FetchType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

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
