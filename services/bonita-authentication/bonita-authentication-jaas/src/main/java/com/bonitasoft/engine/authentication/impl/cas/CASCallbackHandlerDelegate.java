/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl.cas;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;

/**
 * isolate CAS JAAS callback handler behaviour for security/feature purpose
 * 
 * @author Julien Reboul
 * 
 */
public class CASCallbackHandlerDelegate {

    protected CASUtils casUtils = CASUtils.getInstance();

    /**
     * check license and extract ticket from given map
     * 
     * @param credentials
     *            the credentials to extract ticket from
     * @return the CAS security ticket if it exists
     * @throws IllegalAccessException
     *             if the license does not allow to use CAS
     */
    public String getCASTicket(Map<String, Serializable> credentials) {
        if (credentials != null) {
            Serializable ticket = credentials.get(AuthenticationConstants.CAS_TICKET);
            if (ticket != null) {
                casUtils.checkLicense();
                return String.valueOf(ticket);
            }
        }
        return null;
    }

    /**
     * check license and extract service from given map
     * 
     * @param credentials
     *            the credentials to extract service from
     * @return the CAS service if it exists
     * @throws IllegalAccessException
     *             if the license does not allow to use CAS
     */
    public String getCASService(Map<String, Serializable> credentials) {
        if (credentials != null) {
            Serializable service = credentials.get(AuthenticationConstants.CAS_SERVICE);
            if (service != null) {
                casUtils.checkLicense();
                return String.valueOf(service);
            }
        }
        return null;
    }

}
