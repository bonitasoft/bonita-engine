/**
 * Copyright (C) 2018 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
