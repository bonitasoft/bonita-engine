/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.BonitaRuntimeException;

/**
 * Thrown when we try to access on a paused tenant an API method that cannot be called on a paused tenant, or when we
 * try to access on a running tenant an API
 * method that cannot be called on a running tenant.
 *
 * @author Emmanuel Duchastenier
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.exception.TenantStatusException} instead. This
 *             class was only introduced to avoid an API
 *             break of Bonita Engine Subscription version. It will be deleted in next releases.
 */
@Deprecated
public class TenantStatusException extends BonitaRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *        the exception message
     */
    public TenantStatusException(final String message) {
        super(message);
    }

}
