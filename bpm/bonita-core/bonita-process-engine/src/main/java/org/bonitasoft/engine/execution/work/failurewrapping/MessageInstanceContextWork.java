/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Aurelien Pupier
 * @author Celine Souchet
 */
public class MessageInstanceContextWork extends TxInHandleFailureWrappingWork {

    private String messageName;
    private String targetProcess;
    private String targetFlowNode;
    private String eventType;

    public MessageInstanceContextWork(BonitaWork work, String messageName, String targetProcess, String targetFlowNode,
            String eventType) {
        super(work);
        this.messageName = messageName;
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        this.eventType = eventType;
    }

    @Override
    protected void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) {
        sBonitaException.setMessageInstanceNameOnContext(messageName);
        sBonitaException.setMessageInstanceTargetProcessOnContext(targetProcess);
        sBonitaException.setMessageInstanceTargetFlowNodeOnContext(targetFlowNode);
        sBonitaException.setWaitingMessageEventTypeOnContext(eventType);
    }

}
