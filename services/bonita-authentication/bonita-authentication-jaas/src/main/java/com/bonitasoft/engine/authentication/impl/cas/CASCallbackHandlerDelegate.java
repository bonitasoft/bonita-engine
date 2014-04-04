/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
