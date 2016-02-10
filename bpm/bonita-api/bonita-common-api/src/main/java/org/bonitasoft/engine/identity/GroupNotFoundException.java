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

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * thrown when a {@link Group} is not found in the organization
 *
 * @author Kai Lu
 * @author Matthieu Chaffotte
 * @see Group
 * @since 6.0.0
 */
public class GroupNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -3825194748072808390L;

    /**
     * creates a new instance with the cause of the exception
     *
     * @param cause the cause that raised this exception
     */
    public GroupNotFoundException(final Throwable cause) {
        super(cause);
    }

}
