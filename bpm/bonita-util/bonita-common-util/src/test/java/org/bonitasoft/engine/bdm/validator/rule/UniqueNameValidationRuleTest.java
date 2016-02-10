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
package org.bonitasoft.engine.bdm.validator.rule;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static org.bonitasoft.engine.bdm.builder.IndexBuilder.anIndex;
import static org.bonitasoft.engine.bdm.builder.UniqueConstraintBuilder.aUniqueConstraint;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.validator.UniqueNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueNameValidationRuleTest {

    @Mock
    private UniqueNameValidator uniqueNameValidator;

    private UniqueNameValidationRule validationRule;

    @Before
    @SuppressWarnings("unchecked")
    public void initValidationRule() {
        when(uniqueNameValidator.validate(any(Collection.class), any(String.class))).thenReturn(new ValidationStatus());
        validationRule = new UniqueNameValidationRule(uniqueNameValidator);
    }

    private BusinessObject aBoWithConstraints(final UniqueConstraint... uniqueConstraints) {
        BusinessObjectBuilder boBuilder = aBO("aBoWithConstraints");
        for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
            boBuilder = boBuilder.withUniqueConstraint(uniqueConstraint);
        }
        return boBuilder.build();
    }

    private BusinessObject aBoWithIndexes(final Index... indexes) {
        BusinessObjectBuilder bo = aBO("aBoWithIndexes");
        for (final Index index : indexes) {
            bo = bo.withIndex(index);
        }
        return bo.build();
    }

    private ValidationStatus anErrorStatus() {
        final ValidationStatus validationStatus = new ValidationStatus();
        validationStatus.addError("an error");
        return validationStatus;
    }

    @Test
    public void should_validate_names_unicity_for_unique_constraints() {
        final UniqueConstraint uniqueConstraint = aUniqueConstraint().withName("aUniqueConstraint").build();
        final UniqueConstraint uniqueConstraint2 = aUniqueConstraint().withName("anotherUniqueConstraint").build();
        final BusinessObject bo = aBoWithConstraints(uniqueConstraint, uniqueConstraint2);

        validationRule.checkRule(aBOM().withBO(bo).build());

        verify(uniqueNameValidator).validate(asList(uniqueConstraint, uniqueConstraint2), "unique contraints");
    }

    @Test
    public void should_validate_names_unicity_for_indexes() {
        final Index index = anIndex().withName("anIndex").build();
        final Index anotherIndex = anIndex().withName("anotherIndex").build();
        final BusinessObjectModel bom = aBOM().withBO(aBoWithIndexes(index, anotherIndex)).build();

        validationRule.checkRule(bom);

        verify(uniqueNameValidator).validate(asList(index, anotherIndex), "indexes");
    }

    @Test
    public void should_concatenate_validation_errors() {
        final Index index = anIndex().withName("index").build();
        final UniqueConstraint uniqueConstraint = aUniqueConstraint().withName("constraint").build();
        final BusinessObjectModel bom = aBOM().withBO(aBO("bo").withIndex(index).withUniqueConstraint(uniqueConstraint).build()).build();
        when(uniqueNameValidator.validate(eq(asList(index)), anyString())).thenReturn(anErrorStatus());
        when(uniqueNameValidator.validate(eq(asList(uniqueConstraint)), anyString())).thenReturn(anErrorStatus());

        final ValidationStatus checkRule = validationRule.checkRule(bom);

        assertThat(checkRule.getErrors()).hasSize(2);
    }
}
