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

/**
 * Tests the validity of Query parameters
 * 
 * @author Emmanuel Duchastenier
 */
import java.util.Arrays;
import java.util.List;

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

public class QueryParameterValidationRule extends ValidationRule<QueryParameter> {

    public static final List<String> FORBIDDEN_PARAMETER_NAMES = Arrays.asList(BDMQueryUtil.START_INDEX_PARAM_NAME, BDMQueryUtil.MAX_RESULTS_PARAM_NAME);

    public QueryParameterValidationRule() {
        super(QueryParameter.class);
    }

    @Override
    public ValidationStatus validate(final QueryParameter parameter) {
        final ValidationStatus status = new ValidationStatus();
        final String name = parameter.getName();
        if (name == null || name.isEmpty()) {
            status.addError("A parameter must have name");
            return status;
        }
        if (!SourceVersion.isIdentifier(name)) {
            status.addError(name + " is not a valid Java identifier.");
        }
        if (FORBIDDEN_PARAMETER_NAMES.contains(name)) {
            status.addError(name + " is a reserved parameter name. Use a name different from:" + FORBIDDEN_PARAMETER_NAMES);
        }

        if (parameter.getClassName() == null || parameter.getClassName().isEmpty()) {
            status.addError(name + " query parameter must have a classname");
        }
        return status;
    }

}
