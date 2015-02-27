/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

/**
 * Tests the validity of Query parameters
 * 
 * @author Emmanuel Duchastenier
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.bdm.validator.rule.QueryParameterValidationRule} instead.
 */
import java.util.Arrays;
import java.util.List;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.BDMQueryUtil;
import com.bonitasoft.engine.bdm.model.QueryParameter;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

@Deprecated
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
