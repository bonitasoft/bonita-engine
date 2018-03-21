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

import java.util.Collections;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

public class QueryValidationRule extends ValidationRule<Query, ValidationStatus> {

    private static final int MAX_QUERY_NAME_LENGTH = 150;

    public QueryValidationRule() {
        super(Query.class);
    }

    @Override
    public ValidationStatus validate(final Query query) {
        final ValidationStatus status = new ValidationStatus();
        final String name = query.getName();
        if (name == null || name.isEmpty()) {
            status.addError(StatusCode.QUERY_WITHOUT_NAME, "A query must have name");
            return status;
        }
        if (!SourceVersion.isIdentifier(name)) {
            status.addError(StatusCode.INVALID_JAVA_IDENTIFIER_NAME,
                    String.format("%s is not a valid Java identifier.", name),
                    Collections.singletonMap(StatusContext.BDM_ARTIFACT_NAME_KEY, name));
        }
        if (name.length() > MAX_QUERY_NAME_LENGTH) {
            status.addError(StatusCode.QUERY_NAME_LENGTH_TO_HIGH,
                    String.format("%s length must be lower than 150 characters.", name),
                    Collections.singletonMap(StatusContext.BDM_ARTIFACT_NAME_KEY, name));
        }
        if (query.getContent() == null || query.getContent().isEmpty()) {
            status.addError(StatusCode.QUERY_WITHOUT_CONTENT,
                    String.format("%s query must have a content defined", name),
                    Collections.singletonMap(StatusContext.BDM_ARTIFACT_NAME_KEY, name));
        }
        if (query.getReturnType() == null || query.getReturnType().isEmpty()) {
            status.addError(StatusCode.QUERY_WITHOUT_RETURN_TYPE,
                    String.format("%s query must have a return type defined", name),
                    Collections.singletonMap(StatusContext.BDM_ARTIFACT_NAME_KEY, name));
        }
        return status;
    }

}
