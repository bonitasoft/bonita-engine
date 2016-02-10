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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.validator.SQLNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectValidationRule extends ValidationRule<BusinessObject> {

    private static final String[] RESERVED_PACKAGE_PREFIX = { "com.bonitasoft.", "org.bonitasoft." };

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

        for (String reservedPrefix : RESERVED_PACKAGE_PREFIX) {
            if (qualifiedName.startsWith(reservedPrefix)) {
                status.addError(new StringBuilder().append("Package ").append(reservedPrefix).append(" is reserved. Please choose another package name").toString());
            }
        }

        final String simpleName = bo.getSimpleName();
        if (!SourceVersion.isName(qualifiedName) || !sqlNameValidator.isValid(simpleName)) {
            status.addError(new StringBuilder().append(qualifiedName).append(" is not a valid Java qualified name").toString());
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
