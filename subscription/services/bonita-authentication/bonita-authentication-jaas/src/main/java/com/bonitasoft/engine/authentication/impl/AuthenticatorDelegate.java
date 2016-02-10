/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import java.io.Serializable;
import java.util.Map;

/**
 * represents a delegate that will authenticate to an external authentication service
 * 
 * @author Julien Reboul
 * 
 */
public interface AuthenticatorDelegate {

    /**
     * authentication to the underlying external authentication service with the given credentials
     * 
     * @param credentials
     *            the credentials to use to authenticate
     */
    public Map<String, Serializable> authenticate(Map<String, Serializable> credentials);

}
