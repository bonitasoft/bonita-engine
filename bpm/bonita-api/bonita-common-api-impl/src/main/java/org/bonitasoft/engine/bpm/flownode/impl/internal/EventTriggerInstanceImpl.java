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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.internal.BaseElementImpl;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class EventTriggerInstanceImpl extends BaseElementImpl implements EventTriggerInstance {

    private static final long serialVersionUID = 1894571490582208753L;

    private long eventInstanceId;

    public EventTriggerInstanceImpl(final long id, final long eventInstanceId) {
        super();
        setId(id);
        this.eventInstanceId = eventInstanceId;
    }

    @Override
    public long getEventInstanceId() {
        return this.eventInstanceId;
    }

    protected void setEventInstanceId(final long eventInstanceId) {
        this.eventInstanceId = eventInstanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (eventInstanceId ^ (eventInstanceId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventTriggerInstanceImpl other = (EventTriggerInstanceImpl) obj;
        if (eventInstanceId != other.eventInstanceId)
            return false;
        return true;
    }

}
