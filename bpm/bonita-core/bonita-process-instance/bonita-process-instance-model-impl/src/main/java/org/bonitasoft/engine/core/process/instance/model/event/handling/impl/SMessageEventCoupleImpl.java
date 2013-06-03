/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class SMessageEventCoupleImpl implements SMessageEventCouple {

    private static final long serialVersionUID = -2293612457423926547L;

    private SWaitingMessageEventImpl waitingMessage;

    private SMessageInstanceImpl messageInstance;

    private long waitingMessageId;

    private SBPMEventType eventType;

    private long waitingProcessefinitionId;

    private long sendMessageProcessefinitionId;

    private String processName;

    private long waitingFlowNodeDefinitionId;

    private String waitingFlowNodeName;

    private String sendMessageFlowNodeName;

    private String waitingMessageName;

    private long messageId;

    private String messageName;

    private String targetProcess;

    private String targetFlowNode;

    public SMessageEventCoupleImpl() {
    }

    public SMessageEventCoupleImpl(final SWaitingMessageEventImpl waitingMessage, final SMessageInstanceImpl messageInstance) {
        this.waitingMessage = waitingMessage;
        this.messageInstance = messageInstance;
    }

    public SMessageEventCoupleImpl(final long waitingMessageId, final SBPMEventType eventType, final long waitingProcessdefinitionId, final String processName,
            final long waitingFlowNodeDefinitionId, final String waitingFlowNodeName, final String waitingMessageName, final long messageId,
            final String messageName, final String targetProcess, final String targetFlowNode, final long sendMessageProcessefinitionId,
            final String sendMessageFlowNodeName) {
        this.waitingMessageId = waitingMessageId;
        this.eventType = eventType;
        waitingProcessefinitionId = waitingProcessdefinitionId;
        this.processName = processName;
        this.waitingFlowNodeDefinitionId = waitingFlowNodeDefinitionId;
        this.waitingFlowNodeName = waitingFlowNodeName;
        this.waitingMessageName = waitingMessageName;
        this.messageId = messageId;
        this.messageName = messageName;
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        this.sendMessageProcessefinitionId = sendMessageProcessefinitionId;
        this.sendMessageFlowNodeName = sendMessageFlowNodeName;
    }

    @Override
    public SWaitingMessageEvent getWaitingMessage() {
        if (waitingMessage == null) {
            waitingMessage = new SWaitingMessageEventImpl(eventType, waitingProcessefinitionId, processName, waitingFlowNodeDefinitionId, waitingFlowNodeName,
                    waitingMessageName);
            waitingMessage.setId(waitingMessageId);
        }
        return waitingMessage;
    }

    @Override
    public SMessageInstance getMessageInstance() {
        if (messageInstance == null) {
            messageInstance = new SMessageInstanceImpl(messageName, targetProcess, targetFlowNode, sendMessageProcessefinitionId, sendMessageFlowNodeName);
            messageInstance.setId(messageId);
        }
        return messageInstance;
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

    public long getWaitingMessageId() {
        return waitingMessageId;
    }

    public void setWaitingMessageId(final long waitingMessageId) {
        this.waitingMessageId = waitingMessageId;
    }

    public SBPMEventType getEventType() {
        return eventType;
    }

    public void setEventType(final SBPMEventType eventType) {
        this.eventType = eventType;
    }

    public long getWaitingProcessefinitionId() {
        return waitingProcessefinitionId;
    }

    public void setWaitingProcessefinitionId(final long waitingProcessefinitionId) {
        this.waitingProcessefinitionId = waitingProcessefinitionId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(final String processName) {
        this.processName = processName;
    }

    public long getWaitingFlowNodeDefinitionId() {
        return waitingFlowNodeDefinitionId;
    }

    public void setWaitingFlowNodeDefinitionId(final long waitingFlowNodeDefinitionId) {
        this.waitingFlowNodeDefinitionId = waitingFlowNodeDefinitionId;
    }

    public String getFlowNodeName() {
        return waitingFlowNodeName;
    }

    public void setFlowNodeName(final String flowNodeName) {
        waitingFlowNodeName = flowNodeName;
    }

    public String getWaitingMessageName() {
        return waitingMessageName;
    }

    public void setWaitingMessageName(final String waitingMessageName) {
        this.waitingMessageName = waitingMessageName;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(final long messageId) {
        this.messageId = messageId;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(final String messageName) {
        this.messageName = messageName;
    }

    public String getTargetProcess() {
        return targetProcess;
    }

    public void setTargetProcess(final String targetProcess) {
        this.targetProcess = targetProcess;
    }

    public String getTargetFlowNode() {
        return targetFlowNode;
    }

    public void setTargetFlowNode(final String targetFlowNode) {
        this.targetFlowNode = targetFlowNode;
    }

    public long getSendMessageProcessefinitionId() {
        return sendMessageProcessefinitionId;
    }

    public void setSendMessageProcessefinitionId(final long sendMessageProcessefinitionId) {
        this.sendMessageProcessefinitionId = sendMessageProcessefinitionId;
    }

}
