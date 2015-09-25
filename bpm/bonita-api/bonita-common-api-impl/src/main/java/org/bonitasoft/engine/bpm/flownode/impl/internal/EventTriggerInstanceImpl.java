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
import org.bonitasoft.engine.bpm.internal.ProcessBaseElementImpl;

import java.util.Objects;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class EventTriggerInstanceImpl extends ProcessBaseElementImpl implements EventTriggerInstance {

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EventTriggerInstanceImpl that = (EventTriggerInstanceImpl) o;
        return Objects.equals(eventInstanceId, that.eventInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), eventInstanceId);
    }
}
