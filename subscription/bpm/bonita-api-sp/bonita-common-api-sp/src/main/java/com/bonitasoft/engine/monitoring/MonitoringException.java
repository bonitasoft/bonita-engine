/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class MonitoringException extends BonitaException {

    private static final long serialVersionUID = -8253522201752731245L;

    public MonitoringException(final String message) {
        super(message);
    }

    public MonitoringException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
