/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static com.bonitasoft.engine.bdm.builder.IndexBuilder.anIndex;
import static com.bonitasoft.engine.bdm.builder.UniqueConstraintBuilder.aUniqueConstraint;
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

import com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Index;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.UniqueNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

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
