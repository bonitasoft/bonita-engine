/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule.composition;

import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectModelBuilder.aBOM;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aCompositionField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.anAggregationField;
import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class UniquenessCompositionValidationRuleTest {

    private UniquenessCompositionValidationRule rule;

    @Before
    public void initRule() {
        rule = new UniquenessCompositionValidationRule();
    }

    @Test
    public void should_validate_that_a_bom_with_no_relation_fields_is_valid() throws Exception {
        BusinessObjectModel bom = aBOM().withBO(aBO("aBo").build()).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_a_bom_with_no_composition_is_valid() throws Exception {
        BusinessObject aggregated = aBO("aggregated").build();
        BusinessObject bo = aBO("aBo").withField(anAggregationField("aggreg", aggregated)).build();
        BusinessObjectModel bom = aBOM().withBOs(bo, aggregated).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_a_bom_with_composed_bo_in_only_one_bo_is_valid() throws Exception {
        BusinessObject composite = aBO("composite").build();
        BusinessObject bo = aBO("aBo").withField(aCompositionField("composite", composite)).build();
        BusinessObject composite2 = aBO("composite2").build();
        BusinessObject bo2 = aBO("aBo2").withField(aCompositionField("composite2", composite2)).build();
        BusinessObjectModel bom = aBOM().withBOs(bo, composite, bo2, composite2).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_a_bom_with_a_bo_composed_in_two_bos_is_invalid() throws Exception {
        BusinessObject composite = aBO("composite").build();
        BusinessObject firstBO = aBO("firstBO").withField(aCompositionField("boOneComposite", composite)).build();
        BusinessObject secondBO = aBO("secondBO").withField(aCompositionField("boTwoComposite", composite)).build();
        BusinessObjectModel bom = aBOM().withBOs(firstBO, secondBO, composite).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }
}
