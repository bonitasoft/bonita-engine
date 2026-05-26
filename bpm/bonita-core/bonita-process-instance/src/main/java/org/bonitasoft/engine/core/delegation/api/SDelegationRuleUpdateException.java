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
package org.bonitasoft.engine.core.delegation.api;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * Raised by {@code DelegationRuleService.updateRule} when a service-side invariant rejects an
 * update — for example a partial-window inverted-date guard or a cross-field
 * {@code delegate == delegator} violation. These checks require loading the persisted row and
 * therefore cannot be performed by the API layer.
 */
public class SDelegationRuleUpdateException extends SBonitaException {

    private static final long serialVersionUID = 1L;

    public SDelegationRuleUpdateException(final String message) {
        super(message);
    }

    public SDelegationRuleUpdateException(final Throwable cause) {
        super(cause);
    }

    public SDelegationRuleUpdateException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
