/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.validator;

import static org.bonitasoft.engine.bdm.validator.assertion.RuleOfCondition.ruleOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bdm.validator.rule.composition.CyclicCompositionValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.composition.UniquenessCompositionValidationRule;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidatorTest {

    private BusinessObjectModelValidator validator;

    @Before
    public void setUp() {
        validator = new BusinessObjectModelValidator();
    }

    @Test
    public void shouldConstructor_FillListOfRules() {
        assertThat(validator.getRules()).isNotEmpty();
    }

    @Test
    public void shouldValidate_ReturnsAValidStatus() {
        BusinessObjectModel bom = new BusinessObjectModel();
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Car");
        SimpleField nameField = new SimpleField();
        nameField.setName("bmw");
        nameField.setType(FieldType.STRING);
        bo.addField(nameField);
        bom.addBusinessObject(bo);
        assertThat(validator.validate(bom).isOk()).isTrue();
    }

    @Test
    public void shouldValidate_ReturnsAFailedStatus() {
        BusinessObjectModel bom = new BusinessObjectModel();
        BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("org.bonita.Car");
        SimpleField fieldWithTwoErrors = new SimpleField();
        fieldWithTwoErrors.setName("bmw 5");
        bo.getFields().add(fieldWithTwoErrors);
        bom.addBusinessObject(bo);
        ValidationStatus validationStatus = validator.validate(bom);
        assertThat(validationStatus.isOk()).isFalse();
        assertThat(validationStatus.getErrors()).hasSize(2);
    }

    @Test
    public void should_validate_cyclic_composition() {
        assertThat(validator.getRules()).haveAtLeast(1, ruleOf(CyclicCompositionValidationRule.class));
    }

    @Test
    public void should_validate_composition_uniqueness() {
        assertThat(validator.getRules()).haveAtLeast(1, ruleOf(UniquenessCompositionValidationRule.class));
    }
}
