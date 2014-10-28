/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuField;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationMenuCreatorValidator {

    protected final List<String> problems = new ArrayList<String>(2);

    public boolean isValid(final ApplicationMenuCreator creator) {
        problems.clear();
        final Map<ApplicationMenuField, Serializable> fields = creator.getFields();
        if (fields.get(ApplicationMenuField.APPLICATION_ID) == null) {
            problems.add("The applicationId cannot be null");
            return false;
        }
        return true;
    }

    public List<String> getProblems() {
        return Collections.unmodifiableList(problems);
    }
}
