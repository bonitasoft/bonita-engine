/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.commons.exceptions;

import java.io.Serial;

/**
 * Runtime exception signaling that the failed operation may succeed on retry.
 * <p>
 * This exception does not carry retry semantics in its type hierarchy — retryability
 * is determined at runtime by {@code DefaultExceptionRetryabilityEvaluator}, which is
 * configured in {@code bonita-community.xml} with a list of exception classes to retry.
 * {@code SRetryableException} is registered in that list by default.
 * <p>
 * In the Work execution layer ({@code RetryingWorkExecutorService}), throwing this
 * exception triggers automatic retry of the failed Work. Outside that context (e.g.
 * scheduled jobs, direct API calls), callers catch it as a regular runtime exception.
 */
public class SRetryableException extends SBonitaRuntimeException {

    @Serial
    private static final long serialVersionUID = -2007050544702839857L;

    public SRetryableException(final Exception cause) {
        super(cause);
    }

    public SRetryableException(String message) {
        super(message);
    }

    public SRetryableException(String message, Exception cause) {
        super(message, cause);
    }

    @Override
    public synchronized Exception getCause() {
        return (Exception) super.getCause();
    }
}
