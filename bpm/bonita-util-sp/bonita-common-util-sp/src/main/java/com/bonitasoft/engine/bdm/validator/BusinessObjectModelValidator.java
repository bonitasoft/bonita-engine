/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.Query;
import com.bonitasoft.engine.bdm.QueryParameter;
import com.bonitasoft.engine.bdm.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.rule.BusinessObjectModelValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.BusinessObjectValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.FieldValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.QueryValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.UniqueConstraintValidationRule;
import com.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidator {

    private final List<ValidationRule> rules = new ArrayList<ValidationRule>();

    public BusinessObjectModelValidator() {
        rules.add(new BusinessObjectModelValidationRule());
        rules.add(new BusinessObjectValidationRule());
        rules.add(new FieldValidationRule());
        rules.add(new UniqueConstraintValidationRule());
        rules.add(new QueryValidationRule());
        rules.add(new QueryParameterValidationRule());
    }

    public ValidationStatus validate(final BusinessObjectModel bom) {
        final Set<Object> objectsToValidate = buildModelTree(bom);
        final ValidationStatus status = new ValidationStatus();
        for (final Object modelElement : objectsToValidate) {
            for (final ValidationRule rule : rules) {
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
        }
        return objectsToValidate;
    }

    public List<ValidationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

}
