/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator;

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
