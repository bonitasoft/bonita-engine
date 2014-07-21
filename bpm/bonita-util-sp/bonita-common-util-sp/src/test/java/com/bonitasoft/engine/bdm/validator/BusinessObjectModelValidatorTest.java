/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 */
package com.bonitasoft.engine.bdm.validator;

import static com.bonitasoft.engine.bdm.validator.assertion.RuleOfCondition.ruleOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.rule.composition.CyclicCompositionValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.composition.UniquenessCompositionValidationRule;

/**
 * @author Romain Bioteau
 * 
 */
public class BusinessObjectModelValidatorTest {

    private BusinessObjectModelValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new BusinessObjectModelValidator();
    }

    @Test
    public void shouldConstructor_FillListOfRules() throws Exception {
        assertThat(validator.getRules()).isNotEmpty();
    }

    @Test
    public void shouldValidate_ReturnsAValidStatus() throws Exception {
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
    public void shouldValidate_ReturnsAFailedStatus() throws Exception {
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
    public void should_validate_cyclic_composition() throws Exception {
        assertThat(validator.getRules()).haveAtLeast(1, ruleOf(CyclicCompositionValidationRule.class));
    }
    
    @Test
    public void should_validate_composition_uniqueness() throws Exception {
        assertThat(validator.getRules()).haveAtLeast(1, ruleOf(UniquenessCompositionValidationRule.class));
    }
}
