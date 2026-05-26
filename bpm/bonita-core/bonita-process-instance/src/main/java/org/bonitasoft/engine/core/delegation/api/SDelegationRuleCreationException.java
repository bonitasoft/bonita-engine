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
 * Raised by {@code DelegationRuleService.createOrUpdateRule} when a service-side invariant
 * rejects the rule — a cross-field violation such as {@code delegate == delegator} or
 * {@code startDate >= endDate}. These checks live in the service so the rule of "what makes
 * a delegation rule valid on disk" has a single home.
 */
public class SDelegationRuleCreationException extends SBonitaException {

    private static final long serialVersionUID = 1L;

    public SDelegationRuleCreationException(final String message) {
        super(message);
    }

    public SDelegationRuleCreationException(final Throwable cause) {
        super(cause);
    }

    public SDelegationRuleCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
