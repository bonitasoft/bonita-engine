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
import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class CycleCompositionValidationRuleTest {

    private CycleCompositionValidationRule rule;

    @Before
    public void initRule() {
        rule = new CycleCompositionValidationRule();
    }

    @Test
    public void should_validate_that_a_composite_object_cannot_have_one_of_its_ancestor_as_a_child() throws Exception {
        BusinessObject daughter = aBO("daughter").build();
        BusinessObject mother = aBO("mother").withField(aCompositionField("daughter", daughter)).build();
        BusinessObject grandMother = aBO("grandMother").withField(aCompositionField("mother", mother)).build();

        daughter.addField(aCompositionField("forbiddenChild", grandMother));
        BusinessObjectModel bom = aBOM().withBOs(grandMother, mother, daughter).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_a_bo_cannot_compose_itself() throws Exception {
        BusinessObject daughter = aBO("daughter").build();
        daughter.addField(aCompositionField("toto", daughter));
        BusinessObjectModel bom = aBOM().withBOs(daughter).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }
}
