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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Baptiste Mesta
 */
public class SFlowNodeModificationException extends SBonitaException {

    private static final long serialVersionUID = 976164513418178786L;

    public SFlowNodeModificationException(final Throwable cause) {
        super(cause);
    }

    public SFlowNodeModificationException(String message) {
        super(message);
    }

    /**
     * Keeps a cause alongside the diagnostic message. Needed so that a transient failure can carry an
     * {@link org.bonitasoft.engine.commons.exceptions.SRetryableException} cause: the work retryability
     * evaluator walks the whole cause chain, and a message-only exception is classified NOT_RETRYABLE,
     * which fails the flow node instead of retrying it (BPA-167).
     */
    public SFlowNodeModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
