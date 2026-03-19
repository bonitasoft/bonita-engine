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
package org.bonitasoft.console.common.server.login.filter;

/**
 * Unchecked bridge exception used at the servlet-filter level to propagate account lockout
 * across filter-chain boundaries (e.g. from {@code AuthenticationRule} to {@code AuthenticationFilter}).
 * <p>
 * Not to be confused with {@link org.bonitasoft.console.common.server.login.AccountLockedException},
 * which is the engine-level checked exception (extends {@code LoginFailedException}) thrown by
 * {@code LoginManager} when brute-force protection triggers a lockout.
 */
public class AccountLockedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccountLockedRuntimeException(final String message) {
        super(message);
    }

    public AccountLockedRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
