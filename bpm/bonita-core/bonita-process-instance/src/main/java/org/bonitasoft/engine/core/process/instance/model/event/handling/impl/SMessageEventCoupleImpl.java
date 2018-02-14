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
package org.bonitasoft.engine.core.process.instance.model.event.handling.impl;

import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class SMessageEventCoupleImpl implements SMessageEventCouple {

    private static final long serialVersionUID = -2293612457423926547L;

    private long waitingMessageId;

    private SBPMEventType eventType;

    private long messageId;

    public SMessageEventCoupleImpl() {
    }

    public SMessageEventCoupleImpl(final long waitingMessageId, final SBPMEventType eventType, final long messageId) {
        this.waitingMessageId = waitingMessageId;
        this.eventType = eventType;
        this.messageId = messageId;
    }

    @Override
    public long getWaitingMessageId() {
        return this.waitingMessageId;
    }

    @Override
    public long getMessageInstanceId() {
        return messageId;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public void setId(final long id) {
        throw new IllegalArgumentException();
    }

    @Override
    public void setTenantId(final long id) {
        throw new IllegalArgumentException();
    }

    @Override
    public String getDiscriminator() {
        return SMessageEventCouple.class.getName();
    }

	@Override
    public SBPMEventType getWaitingMessageEventType() {
	    return eventType;
    }

    @Override
    public String toString() {
        return "SMessageEventCoupleImpl{" +
                "waitingMessageId=" + waitingMessageId +
                ", eventType=" + eventType +
                ", messageId=" + messageId +
                '}';
    }
}
