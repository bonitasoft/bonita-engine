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
import static org.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class CyclicCompositionValidationRuleTest {

    private CyclicCompositionValidationRule rule;

    @Before
    public void initRule() {
        rule = new CyclicCompositionValidationRule();
    }

    @Test
    public void should_validate_that_a_composite_object_cannot_have_one_of_its_ancestor_as_a_child() {
        final BusinessObject daughter = aBO("daughter").build();
        final BusinessObject mother = aBO("mother").withField(aCompositionField("daughter", daughter)).build();
        final BusinessObject grandMother = aBO("grandMother").withField(aCompositionField("mother", mother)).build();

        daughter.addField(aCompositionField("forbiddenChild", grandMother));
        final BusinessObjectModel bom = aBOM().withBOs(grandMother, mother, daughter).build();

        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_a_bo_cannot_compose_itself() {
        final BusinessObject daughter = aBO("daughter").build();
        daughter.addField(aCompositionField("toto", daughter));
        final BusinessObjectModel bom = aBOM().withBOs(daughter).build();

        final ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }
    
    @Test
    public void should_validate_that_a_bom_with_the_same_bo_composed_two_times_is_invalid(){
        //given
        final BusinessObject wheel = aBO("wheel").build();
        final BusinessObject bycicle = aBO("bicycle").withField(aCompositionField("frontwheel",wheel)).withField(aCompositionField("backwheel",wheel)).build();
        final BusinessObjectModel bom = aBOM().withBOs(wheel,bycicle).build();
        //when
        ValidationStatus validationStatus = rule.validate(bom);
        //then
        assertThat(validationStatus).isNotOk();
        assertThat(validationStatus).hasError("Business object " + "wheel" + " has a circular composition reference to itself or is referenced several times in the object " + "bicycle");
    }
}
