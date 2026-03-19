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
package org.bonitasoft.console.common.server.login;

import org.bonitasoft.web.server.login.LoginFailureTracker;

/**
 * Thrown when a login attempt is rejected because the user account has been temporarily locked
 * due to too many consecutive failed authentication attempts.
 * <p>
 * This exception is raised by {@link LoginManager} when the {@link LoginFailureTracker} detects that the maximum
 * number of allowed failures has been exceeded for a given username. The account remains locked until the configured
 * lockout duration expires.
 *
 * @see LoginFailureTracker
 * @see LoginManager
 */
public class AccountLockedException extends LoginFailedException {

    private static final long serialVersionUID = 1L;

    public AccountLockedException(final String message) {
        super(message);
    }
}
