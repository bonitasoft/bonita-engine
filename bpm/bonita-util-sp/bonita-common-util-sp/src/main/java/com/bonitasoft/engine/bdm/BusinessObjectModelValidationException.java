/*******************************************************************************
 * Copyright (C) 2013, 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidationException extends Exception {

    private final ValidationStatus validationStatus;

    public BusinessObjectModelValidationException(final ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        for (final String errorMessage : validationStatus.getErrors()) {
            sb.append("\n- ");
            sb.append(errorMessage);
        }
        return sb.toString();
    }

    private static final long serialVersionUID = 1L;

}
