/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.message;

import java.util.Map;

/**
 * @author Emmanuel Duchastenier
 */
public class BPMMessage {

    private String messageName;
    private String targetProcess;
    private String targetFlowNode;
    private Map<String, BPMMessageValue> messageContent;
    private Map<String, BPMMessageValue> correlations;

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getTargetProcess() {
        return targetProcess;
    }

    public void setTargetProcess(String targetProcess) {
        this.targetProcess = targetProcess;
    }

    public String getTargetFlowNode() {
        return targetFlowNode;
    }

    public void setTargetFlowNode(String targetFlowNode) {
        this.targetFlowNode = targetFlowNode;
    }

    public Map<String, BPMMessageValue> getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(Map<String, BPMMessageValue> messageContent) {
        this.messageContent = messageContent;
    }

    public Map<String, BPMMessageValue> getCorrelations() {
        return correlations;
    }

    public void setCorrelations(Map<String, BPMMessageValue> correlations) {
        this.correlations = correlations;
    }

}
