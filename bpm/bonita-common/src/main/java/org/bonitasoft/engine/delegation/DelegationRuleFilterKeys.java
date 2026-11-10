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
package org.bonitasoft.engine.delegation;

/**
 * Shared filter-key constants for delegation-rule searches. The keys are referenced both by the
 * subscription public API (e.g. {@code DelegationRuleSearchDescriptor}) and by the community-side
 * service implementation that consumes them — a single source of truth keeps the two sides from
 * drifting apart.
 */
public final class DelegationRuleFilterKeys {

    /**
     * Virtual filter key for the delegation-rule lifecycle status (scheduled / active / expired).
     * Translated at the service layer into date predicates against {@code startDate} / {@code endDate}.
     */
    public static final String STATUS = "status";

    private DelegationRuleFilterKeys() {
        // utility class
    }
}
