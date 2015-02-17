/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

@Deprecated
public class QueryValidationRule extends ValidationRule<Query> {

    private static final int MAX_QUERY_NAME_LENGTH = 150;

    public QueryValidationRule() {
        super(Query.class);
    }

    @Override
    public ValidationStatus validate(final Query query) {
        final ValidationStatus status = new ValidationStatus();
        final String name = query.getName();
        if (name == null || name.isEmpty()) {
            status.addError("A query must have name");
            return status;
        }
        if (!SourceVersion.isIdentifier(name)) {
            status.addError(name + " is not a valid Java identifier.");
        }
        if (name.length() > MAX_QUERY_NAME_LENGTH) {
            status.addError(name + " length must be lower than 150 characters.");
        }
        if (query.getContent() == null || query.getContent().isEmpty()) {
            status.addError(name + " query must have a content defined");
        }
        if (query.getReturnType() == null || query.getReturnType().isEmpty()) {
            status.addError(name + " query must have a return type defined");
        }

        return status;
    }

}
