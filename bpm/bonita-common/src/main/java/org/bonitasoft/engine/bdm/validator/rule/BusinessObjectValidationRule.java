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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
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
public class BusinessObjectValidationRule extends ValidationRule<BusinessObject, ValidationStatus> {

    private static final String[] RESERVED_PACKAGE_PREFIX = { "com.bonitasoft.", "org.bonitasoft." };

    private static final int MAX_TABLE_NAME_LENGTH = 30;

    private final SQLNameValidator sqlNameValidator;

    public BusinessObjectValidationRule() {
        super(BusinessObject.class);
        sqlNameValidator = new SQLNameValidator(MAX_TABLE_NAME_LENGTH);
    }

    @Override
    public ValidationStatus validate(final BusinessObject bo) {
        final ValidationStatus status = new ValidationStatus();
        final String qualifiedName = bo.getQualifiedName();
        if (qualifiedName == null) {
            status.addError(StatusCode.BUSINESS_OBJECT_WITHOUT_NAME, "A Business Object must have a qualified name");
            return status;
        }

        for (String reservedPrefix : RESERVED_PACKAGE_PREFIX) {
            if (qualifiedName.startsWith(reservedPrefix)) {
                status.addError(StatusCode.RESERVED_PACKAGE_NAME,
                        String.format("Package %s is reserved. Please choose another package name", reservedPrefix),
                        Collections.singletonMap(StatusContext.BDM_ARTIFACT_NAME_KEY, reservedPrefix));
            }
        }

        final String simpleName = bo.getSimpleName();
        if (!SourceVersion.isName(qualifiedName) || !sqlNameValidator.isValid(simpleName)) {
            status.addError(StatusCode.INVALID_JAVA_IDENTIFIER_NAME,
                    String.format("%s is not a valid Java qualified name", qualifiedName),
                    Collections.singletonMap(StatusContext.BUSINESS_OBJECT_NAME_KEY, qualifiedName));
            return status;
        }

        if (simpleName.contains("_")) {
            status.addError(StatusCode.INVALID_CHARACTER_IN_BUSINESS_OBJECT_NAME,
                    "_ is a forbidden character in business object's name");
        }

        if (bo.getFields().isEmpty()) {
            status.addError(StatusCode.BUSINESS_OBJECT_WITHOUT_FIELD,
                    String.format("%s must have at least one field declared", qualifiedName),
                    Collections.singletonMap(StatusContext.BUSINESS_OBJECT_NAME_KEY, qualifiedName));
        }

        validateConstraints(bo, status);
        validateQueries(bo, status);
        return status;
    }

    private void validateQueries(final BusinessObject bo, final ValidationStatus status) {
        final Set<String> queryNames = BDMQueryUtil.getAllProvidedQueriesNameForBusinessObject(bo);
        Map<String, Serializable> context = new HashMap<>();
        context.put(StatusContext.BUSINESS_OBJECT_NAME_KEY, bo.getQualifiedName());
        for (final Query q : bo.getQueries()) {
            if (queryNames.contains(q.getName())) {
                context.put(StatusContext.BDM_ARTIFACT_NAME_KEY, q.getName());
                status.addError(StatusCode.DUPLICATE_QUERY_NAME,
                        "The query named \"" + q.getName() + "\" already exists for " + bo.getQualifiedName(),
                        context);
            } else {
                queryNames.add(q.getName());
            }
        }
    }

    private void validateConstraints(final BusinessObject bo, final ValidationStatus status) {
        final Set<String> constraintNames = new HashSet<>();
        Map<String, Serializable> context = new HashMap<>();
        context.put(StatusContext.BUSINESS_OBJECT_NAME_KEY, bo.getQualifiedName());
        for (final UniqueConstraint uc : bo.getUniqueConstraints()) {
            String name = uc.getName();
            if (constraintNames.contains(name)) {
                context.put(StatusContext.BDM_ARTIFACT_NAME_KEY, name);
                status.addError(StatusCode.DUPLICATE_CONSTRAINT_NAME,
                        "The constraint named \"" + name + "\" already exists for " + bo.getQualifiedName(),
                        context);
            } else {
                constraintNames.add(name);
            }
            List<String> fieldNames = uc.getFieldNames();
            if (fieldNames != null) {
                for (final String fName : fieldNames) {
                    if (getField(bo, fName) == null) {
                        context.put(StatusContext.BDM_ARTIFACT_NAME_KEY, fName);
                        status.addError(StatusCode.UNKNOWN_FIELD_IN_CONSTRAINT,
                                String.format("The field named %s does not exist in %s", fName, bo.getQualifiedName()),
                                context);
                    }
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
