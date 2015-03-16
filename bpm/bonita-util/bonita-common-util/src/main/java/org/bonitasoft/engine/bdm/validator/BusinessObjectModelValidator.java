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
package org.bonitasoft.engine.bdm.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.validator.rule.BusinessObjectModelValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.BusinessObjectValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.FieldValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.IndexValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.QueryValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.SimpleFieldValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.UniqueConstraintValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.ValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.composition.CyclicCompositionValidationRule;
import org.bonitasoft.engine.bdm.validator.rule.composition.UniquenessCompositionValidationRule;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidator {

    private final List<ValidationRule<?>> rules = new ArrayList<ValidationRule<?>>();

    public BusinessObjectModelValidator() {
        rules.add(new BusinessObjectModelValidationRule());
        rules.add(new BusinessObjectValidationRule());
        rules.add(new FieldValidationRule());
        rules.add(new SimpleFieldValidationRule());
        rules.add(new UniqueConstraintValidationRule());
        rules.add(new IndexValidationRule());
        rules.add(new QueryValidationRule());
        rules.add(new QueryParameterValidationRule());
        rules.add(new UniquenessCompositionValidationRule());
        rules.add(new CyclicCompositionValidationRule());
    }

    public ValidationStatus validate(final BusinessObjectModel bom) {
        final Set<Object> objectsToValidate = buildModelTree(bom);
        final ValidationStatus status = new ValidationStatus();
        for (final Object modelElement : objectsToValidate) {
            for (final ValidationRule<?> rule : rules) {
                if (rule.appliesTo(modelElement)) {
                    status.addValidationStatus(rule.checkRule(modelElement));
                }
            }
        }
        return status;
    }

    private Set<Object> buildModelTree(final BusinessObjectModel bom) {
        final Set<Object> objectsToValidate = new HashSet<Object>();
        objectsToValidate.add(bom);
        for (final BusinessObject bo : bom.getBusinessObjects()) {
            objectsToValidate.add(bo);
            for (final Field f : bo.getFields()) {
                objectsToValidate.add(f);
            }
            final List<UniqueConstraint> uniqueConstraints = bo.getUniqueConstraints();
            for (final UniqueConstraint uc : uniqueConstraints) {
                objectsToValidate.add(uc);
            }
            final List<Query> queries = bo.getQueries();
            for (final Query q : queries) {
                objectsToValidate.add(q);
                for (final QueryParameter p : q.getQueryParameters()) {
                    objectsToValidate.add(p);
                }
            }
            for (final Index index : bo.getIndexes()) {
                objectsToValidate.add(index);
            }
        }
        return objectsToValidate;
    }

    public List<ValidationRule<?>> getRules() {
        return Collections.unmodifiableList(rules);
    }

}
