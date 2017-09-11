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
package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.identity.model.SUser;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserNotFoundException extends SIdentityException {

    private static final long serialVersionUID = -8190385127379005323L;

    public SUserNotFoundException(final SUser user) {
        this(user.getUserName());
        setUserIdOnContext(user.getId());
    }

    public SUserNotFoundException(final long userId) {
        super("Can't find the user");
        setUserIdOnContext(userId);
    }

    public SUserNotFoundException(final String userName) {
        super("?", userName);
    }

    public SUserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SUserNotFoundException(final Throwable cause) {
        super(cause);
    }

}
