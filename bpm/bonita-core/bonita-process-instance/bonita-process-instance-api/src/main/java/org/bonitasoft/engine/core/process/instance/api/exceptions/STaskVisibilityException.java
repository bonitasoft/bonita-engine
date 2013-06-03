/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Emmanuel Duchastenier
 */
public class STaskVisibilityException extends SBonitaException {

    private static final long serialVersionUID = 7406562594163981383L;

    /**
     * @param taskInstanceId
     *            the ID of the task whose visibility is being
     * @param userId
     *            the ID of the user whose Task visibility is associated to
     * @param taskVisibilityAction
     *            the String name of the action being performed on Task visibility that performed this Exception (Creation / Modification / Deletion)
     */
    public STaskVisibilityException(final long taskInstanceId, final long userId, final String taskVisibilityAction) {
        super("Task visibility " + taskVisibilityAction + " failed on task " + taskInstanceId + " for user " + userId);
    }

    public STaskVisibilityException(final long taskInstanceId, final long userId, final String taskVisibilityAction, final Throwable e) {
        super("Task visibility " + taskVisibilityAction + " failed on task " + taskInstanceId + " for user " + userId, e);
    }

    public STaskVisibilityException(final String message) {
        super(message);
    }

    public STaskVisibilityException(final String message, final Throwable e) {
        super(message, e);
    }

}
