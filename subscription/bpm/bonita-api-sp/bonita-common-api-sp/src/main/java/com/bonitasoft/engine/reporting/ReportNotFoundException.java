/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.reporting;

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * @author Matthieu Chaffotte
 */
public class ReportNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 2842457668242337487L;

    public ReportNotFoundException(final Throwable cause) {
        super(cause);
    }

}
