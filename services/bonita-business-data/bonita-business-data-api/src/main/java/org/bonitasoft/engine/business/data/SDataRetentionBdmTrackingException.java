/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * Checked exception thrown by {@link DataRetentionBdmTrackingService} when a
 * tracking operation (create, upsert, update, delete) fails.
 * <p>Wraps lower-level persistence exceptions so that callers in modules that
 * do not depend on {@code bonita-persistence} can still handle tracking errors.
 */
public class SDataRetentionBdmTrackingException extends SBonitaException {

    public SDataRetentionBdmTrackingException(String message) {
        super(message);
    }

    public SDataRetentionBdmTrackingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SDataRetentionBdmTrackingException(Throwable cause) {
        super(cause);
    }
}
