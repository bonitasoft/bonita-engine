/*
 * Copyright (C) 2018 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */

package org.bonitasoft.engine.bdm.validator.rule.composition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aCompositionField;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.anAggregationField;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Danila Mazour
 */
public class AggregationAndCompositionValidationRuleTest {

    private AggregationAndCompositionValidationRule rule;

    @Before
    public void setUp() {
        rule = new AggregationAndCompositionValidationRule();
    }

    @Test
    public void shouldDetectAggregationAndComposition() {

        final BusinessObject daughter = aBO("daughter").build();
        final BusinessObject mother = aBO("mother").withField(aCompositionField("daughter", daughter)).build();
        final BusinessObject grandMother = aBO("grandMother").withField(anAggregationField("daughter", daughter)).build();
        final BusinessObjectModel bom = aBOM().withBOs(grandMother, mother, daughter).build();
        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus.isOk()).isTrue();
        assertThat(validationStatus.getErrors()).isEmpty();
        assertThat(validationStatus.getWarnings().size()).isEqualTo(1);
        assertThat(validationStatus.getWarnings().get(0)).isEqualTo(
                "The object daughter is referenced both in composition and in aggregation. This may lead to runtime errors and may lead to unpredictable behaviour of the AccessControl configuration.");

    }
}
