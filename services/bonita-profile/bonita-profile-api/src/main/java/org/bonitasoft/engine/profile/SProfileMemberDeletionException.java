/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 */
public class SProfileMemberDeletionException extends SBonitaException {

    private static final long serialVersionUID = -733208211528042662L;

    public SProfileMemberDeletionException(final String message) {
        super(message);
    }

    public SProfileMemberDeletionException(final Throwable cause) {
        super(cause);
    }

    public SProfileMemberDeletionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
