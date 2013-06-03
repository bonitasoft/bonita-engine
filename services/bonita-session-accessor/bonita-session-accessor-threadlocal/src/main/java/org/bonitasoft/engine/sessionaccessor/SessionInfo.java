/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.sessionaccessor;

import java.util.Map.Entry;

/**
 * @author Matthieu Chaffotte
 */
public class SessionInfo implements Entry<Long, Long> {

    private final Long sessionId;

    private final Long tenantId;

    public SessionInfo(final Long sessionId, final Long tenantId) {
        super();
        this.sessionId = sessionId;
        this.tenantId = tenantId;
    }

    @Override
    public Long getKey() {
        return sessionId;
    }

    @Override
    public Long getValue() {
        return tenantId;
    }

    @Override
    public Long setValue(final Long arg0) {
        throw new UnsupportedOperationException("Is is not allowed to update the value of the entry");
    }

}
