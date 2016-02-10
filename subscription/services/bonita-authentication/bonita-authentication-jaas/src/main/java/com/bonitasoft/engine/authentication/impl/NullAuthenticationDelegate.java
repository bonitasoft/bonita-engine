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
 * Provides a no op delegate when no authentication delegation is required
 * 
 * @author Julien Reboul
 *
 */
public class NullAuthenticationDelegate implements AuthenticatorDelegate {

    @Override
    public Map<String, Serializable> authenticate(Map<String, Serializable> credentials) {
        return credentials;
    }

}
