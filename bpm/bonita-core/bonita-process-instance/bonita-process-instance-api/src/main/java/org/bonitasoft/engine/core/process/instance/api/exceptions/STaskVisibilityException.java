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
package org.bonitasoft.engine.core.process.instance.api.exceptions;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class STaskVisibilityException extends SBonitaException {

    private static final long serialVersionUID = 7406562594163981383L;

    public STaskVisibilityException(final String message) {
        super(message);
    }

    public STaskVisibilityException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public STaskVisibilityException(final String message, final long activityInstanceId, final long userId) {
        super(message);
        setFlowNodeDefinitionIdOnContext(activityInstanceId);
        setUserIdOnContext(userId);
    }

    /**
     * @param taskInstanceId
     *            the ID of the task whose visibility is being
     * @param userId
     *            the ID of the user whose Task visibility is associated to
     */
    public STaskVisibilityException(final String message, final long activityInstanceId, final long userId, final Throwable cause) {
        super(message, cause);
        setFlowNodeDefinitionIdOnContext(activityInstanceId);
        setUserIdOnContext(userId);
    }

    public STaskVisibilityException(final String message, final long activityInstanceId) {
        super(message);
        setFlowNodeDefinitionIdOnContext(activityInstanceId);
    }

    public STaskVisibilityException(final String message, final long activityInstanceId, final Throwable cause) {
        super(message, cause);
        setFlowNodeDefinitionIdOnContext(activityInstanceId);
    }

}
