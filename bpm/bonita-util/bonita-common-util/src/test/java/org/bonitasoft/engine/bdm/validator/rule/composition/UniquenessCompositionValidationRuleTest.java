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
package org.bonitasoft.engine.bdm.validator.rule.composition;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aCompositionField;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.anAggregationField;
import static org.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

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
    public void should_validate_that_a_bom_with_no_relation_fields_is_valid() {
        final BusinessObjectModel bom = aBOM().withBO(aBO("aBo").build()).build();

        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_a_bom_with_no_composition_is_valid() {
        final BusinessObject aggregated = aBO("aggregated").build();
        final BusinessObject bo = aBO("aBo").withField(anAggregationField("aggreg", aggregated)).build();
        final BusinessObjectModel bom = aBOM().withBOs(bo, aggregated).build();

        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_a_bom_with_composed_bo_in_only_one_bo_is_valid() {
        final BusinessObject composite = aBO("composite").build();
        final BusinessObject bo = aBO("aBo").withField(aCompositionField("composite", composite)).build();
        final BusinessObject composite2 = aBO("composite2").build();
        final BusinessObject bo2 = aBO("aBo2").withField(aCompositionField("composite2", composite2)).build();
        final BusinessObjectModel bom = aBOM().withBOs(bo, composite, bo2, composite2).build();

        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_a_bom_with_a_bo_composed_in_two_bos_is_invalid() {
        final BusinessObject composite = aBO("composite").build();
        final BusinessObject firstBO = aBO("firstBO").withField(aCompositionField("boOneComposite", composite)).build();
        final BusinessObject secondBO = aBO("secondBO").withField(aCompositionField("boTwoComposite", composite)).build();
        final BusinessObjectModel bom = aBOM().withBOs(firstBO, secondBO, composite).build();

        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }
}
