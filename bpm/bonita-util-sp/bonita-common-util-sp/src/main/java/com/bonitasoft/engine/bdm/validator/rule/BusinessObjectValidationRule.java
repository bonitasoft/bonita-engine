/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bdm.validator.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.BDMQueryUtil;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.Query;
import com.bonitasoft.engine.bdm.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.SQLNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectValidationRule implements ValidationRule {

    private static final int MAX_TABLENAME_LENGTH = 30;

    private SQLNameValidator sqlNameValidator;

    public BusinessObjectValidationRule() {
        sqlNameValidator = new SQLNameValidator(MAX_TABLENAME_LENGTH);
    }

    @Override
    public boolean appliesTo(final Object modelElement) {
        return modelElement instanceof BusinessObject;
    }

    @Override
    public ValidationStatus checkRule(final Object modelElement) {
        if (!appliesTo(modelElement)) {
            throw new IllegalArgumentException(BusinessObjectValidationRule.class.getName() + " doesn't handle validation for "
                    + modelElement.getClass().getName());
        }
        final BusinessObject bo = (BusinessObject) modelElement;

        final ValidationStatus status = new ValidationStatus();
        final String qualifiedName = bo.getQualifiedName();
        if (qualifiedName == null) {
            status.addError("A Business Object must have a qualified name");
            return status;
        }
        if (!SourceVersion.isName(qualifiedName) || !sqlNameValidator.isValid(getSimpleName(qualifiedName))) {
            status.addError(qualifiedName + " is not a valid Java qualified name");
            return status;
        }
        if (bo.getFields().isEmpty()) {
            status.addError(qualifiedName + " must have at least one field declared");
        }
        Set<String> constraintNames = new HashSet<String>();
        Set<String> queryNames = new HashSet<String>();
        for (final UniqueConstraint uc : bo.getUniqueConstraints()) {
            if (constraintNames.contains(uc.getName())) {
                status.addError("The constraint named \"" + uc.getName() + "\" already exists for " + bo.getQualifiedName());
            } else {
                constraintNames.add(uc.getName());
            }

            queryNames.add(BDMQueryUtil.createQueryNameForUniqueConstraint(bo.getQualifiedName(), uc));
            for (final String fName : uc.getFieldNames()) {
                final Field field = getField(bo, fName);
                if (field == null) {
                    status.addError("The field named " + fName + " does not exist in " + bo.getQualifiedName());
                }
            }
        }

        for (final Query q : bo.getQueries()) {
            if (queryNames.contains(q.getName())) {
                status.addError("The query named \"" + q.getName() + "\" already exists for " + bo.getQualifiedName());
            } else {
                queryNames.add(q.getName());
            }
        }

        return status;
    }

    private String getSimpleName(String qualifiedName) {
        String simpleName = qualifiedName;
        if (simpleName.indexOf(".") != -1) {
            String[] split = simpleName.split("\\.");
            simpleName = split[split.length - 1];
        }
        return simpleName;
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
