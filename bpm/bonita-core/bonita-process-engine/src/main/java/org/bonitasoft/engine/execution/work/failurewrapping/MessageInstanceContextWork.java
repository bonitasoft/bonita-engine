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
package org.bonitasoft.engine.execution.work.failurewrapping;

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Aurelien Pupier
 * @author Celine Souchet
 */
public class MessageInstanceContextWork extends TxInHandleFailureWrappingWork {

    private static final long serialVersionUID = 8250011422595020879L;

    private final SMessageInstance messageInstance;

    private final SWaitingMessageEvent waitingMessageEvent;

    public MessageInstanceContextWork(final BonitaWork work, final SMessageInstance messageInstance, final SWaitingMessageEvent waitingMessageEvent) {
        super(work);
        this.messageInstance = messageInstance;
        this.waitingMessageEvent = waitingMessageEvent;
    }

    @Override
    protected void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) {
        sBonitaException.setMessageInstanceNameOnContext(messageInstance.getMessageName());
        sBonitaException.setMessageInstanceTargetProcessOnContext(messageInstance.getTargetProcess());
        sBonitaException.setMessageInstanceTargetFlowNodeOnContext(messageInstance.getTargetFlowNode());
        sBonitaException.setWaitingMessageEventTypeOnContext(waitingMessageEvent.getEventType().name());
    }

}
