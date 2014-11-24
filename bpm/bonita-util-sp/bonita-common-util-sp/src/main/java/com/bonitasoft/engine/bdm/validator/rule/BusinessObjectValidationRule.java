/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.BDMQueryUtil;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.validator.SQLNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectValidationRule extends ValidationRule<BusinessObject> {

    private static final int MAX_TABLENAME_LENGTH = 30;

    private final SQLNameValidator sqlNameValidator;

    public BusinessObjectValidationRule() {
        super(BusinessObject.class);
        sqlNameValidator = new SQLNameValidator(MAX_TABLENAME_LENGTH);
    }

    @Override
    public ValidationStatus validate(final BusinessObject bo) {
        final ValidationStatus status = new ValidationStatus();
        final String qualifiedName = bo.getQualifiedName();
        if (qualifiedName == null) {
            status.addError("A Business Object must have a qualified name");
            return status;
        } 
        
        if (qualifiedName.startsWith("com.bonitasoft")) {
            status.addError("Package com.bonitasoft is reserved. Please choose another package name");
        }
        
        String simpleName = bo.getSimpleName();
        if (!SourceVersion.isName(qualifiedName) || !sqlNameValidator.isValid(simpleName)) {
            status.addError(qualifiedName + " is not a valid Java qualified name");
            return status;
        }
        
        if (simpleName.contains("_")) {
            status.addError("_ is a forbidden character in business object's name");
        }
        
        if (bo.getFields().isEmpty()) {
            status.addError(qualifiedName + " must have at least one field declared");
        }
        
        validateConstraints(bo, status);
        validateQueries(bo, status);
        return status;
    }

    private void validateQueries(final BusinessObject bo, final ValidationStatus status) {
        final Set<String> queryNames = BDMQueryUtil.getAllProvidedQueriesNameForBusinessObject(bo);
        for (final Query q : bo.getQueries()) {
            if (queryNames.contains(q.getName())) {
                status.addError("The query named \"" + q.getName() + "\" already exists for " + bo.getQualifiedName());
            } else {
                queryNames.add(q.getName());
            }
        }
    }

    private void validateConstraints(final BusinessObject bo, final ValidationStatus status) {
        final Set<String> constraintNames = new HashSet<String>();
        for (final UniqueConstraint uc : bo.getUniqueConstraints()) {
            if (constraintNames.contains(uc.getName())) {
                status.addError("The constraint named \"" + uc.getName() + "\" already exists for " + bo.getQualifiedName());
            } else {
                constraintNames.add(uc.getName());
            }
            for (final String fName : uc.getFieldNames()) {
                final Field field = getField(bo, fName);
                if (field == null) {
                    status.addError("The field named " + fName + " does not exist in " + bo.getQualifiedName());
                }
            }
        }
    }

    private Field getField(final BusinessObject bo, final String name) {
        Field found = null;
        int index = 0;
        final List<Field> fields = bo.getFields();
        while (found == null && index < fields.size()) {
            final Field field = bo.getFields().get(index);
            if (field.getName().equals(name)) {
                found = field;
            }
            index++;
        }
        return found;
    }
}
