/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.api.exceptions;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class SRefBusinessDataInstanceNotFoundException extends SBonitaException {

    private static final long serialVersionUID = -5163481117317685640L;

    public SRefBusinessDataInstanceNotFoundException(final long processInstanceId, final String name) {
        super("Unable to find a reference to a business data named '" + name + "' of process instance " + processInstanceId);
    }

}
