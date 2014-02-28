/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work.failurehandling;

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Aurelien Pupier
 *
 */
public class FailureHandlingMessageInstanceContextWork extends FailureHandlingBonitaWork {

	private static final long serialVersionUID = 8250011422595020879L;
	private final SMessageInstance messageInstance;
	private final SWaitingMessageEvent waitingMessageEvent;

	public FailureHandlingMessageInstanceContextWork(BonitaWork work, SMessageInstance messageInstance, SWaitingMessageEvent waitingMessageEvent) {
		super(work);
		this.messageInstance = messageInstance;
		this.waitingMessageEvent = waitingMessageEvent;
	}

	@Override
	protected void setExceptionContext(SBonitaException sBonitaException, Map<String, Object> context) throws SBonitaException {
		sBonitaException.setMessageInstanceName(messageInstance.getMessageName());
		sBonitaException.setMessageInstanceTargetProcess(messageInstance.getTargetProcess());
		sBonitaException.setMessageInstanceTargetFlowNode(messageInstance.getTargetFlowNode());
		sBonitaException.setWaitingMessageEventType(waitingMessageEvent.getEventType().name());
	}

}
