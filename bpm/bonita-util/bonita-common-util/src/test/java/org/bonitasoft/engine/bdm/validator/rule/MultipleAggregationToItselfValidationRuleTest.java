/*
 * Copyright (C) 2018 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */

package org.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static org.bonitasoft.engine.bdm.model.field.RelationField.FetchType.EAGER;

import org.bonitasoft.engine.bdm.builder.FieldBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Danila Mazour
 */
public class MultipleAggregationToItselfValidationRuleTest {

    private MultipleAggregationToItselfValidationRule rule;

    @Before
    public void setUp() {
        rule = new MultipleAggregationToItselfValidationRule();
    }

    @Test
    public void shouldDetectMultipleAggregationToItself() {
        BusinessObject daughter = aBO("daughter").build();
        daughter = aBO("daughter")
                .withField(new FieldBuilder.RelationFieldBuilder().withName("daughter").aggregation().multiple().referencing(daughter).fetchType(
                        EAGER))
                .build();

        final BusinessObjectModel bom = aBOM().withBOs(daughter).build();
        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus.isOk()).isFalse();
        assertThat(validationStatus.getErrors()).hasSize(1);
        assertThat(validationStatus.getWarnings()).hasSize(0);
        assertThat(validationStatus.getErrors().get(0)).isEqualTo("The object daughter is referencing itself in a multiple aggregation relation.");

    }
}
