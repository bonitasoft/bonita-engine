/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean;

/**
 * @author Christophe Havard
 */
public class MBeanStartException extends Exception {

    private static final long serialVersionUID = 7747917234880930872L;

    public MBeanStartException(final Exception e) {
        super(e);
    }

    public MBeanStartException(final String message) {
        super(message);
    }

}
