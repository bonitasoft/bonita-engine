/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.validator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Romain Bioteau
 */
public class ValidationStatus {

    private final List<String> errorList;

    public ValidationStatus() {
        errorList = new ArrayList<String>();
    }

    public void addError(final String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            throw new IllegalArgumentException("errorMessage cannot be null or empty");
        }
        errorList.add(errorMessage);
    }

    public boolean isOk() {
        return errorList.isEmpty();
    }

    public void addValidationStatus(final ValidationStatus status) {
        errorList.addAll(status.getErrors());
    }

    public List<String> getErrors() {
        return errorList;
    }

}
