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
package org.bonitasoft.engine.core.process.instance.model.event.trigger.impl;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;

/**
 * @author Elias Ricken de Medeiros
 */
public class SThrowMessageEventTriggerInstanceImpl extends SEventTriggerInstanceImpl implements SThrowMessageEventTriggerInstance {

    private static final long serialVersionUID = 965073794137215054L;

    private String messageName;

    private String targetProcess;

    private String targetFlowNode;

    public SThrowMessageEventTriggerInstanceImpl() {
    }

    public SThrowMessageEventTriggerInstanceImpl(final long eventInstanceId, final String messageName, final String targetProcess, final String targetFlowNode) {
        super(eventInstanceId);
        this.messageName = messageName;
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
    }

    @Override
    public String getDiscriminator() {
        return SThrowMessageEventTriggerInstance.class.getName();
    }

    @Override
    public String getMessageName() {
        return messageName;
    }

    @Override
    public String getTargetProcess() {
        return targetProcess;
    }

    @Override
    public String getTargetFlowNode() {
        return targetFlowNode;
    }

    public void setMessageName(final String messageName) {
        this.messageName = messageName;
    }

    public void setTargetProcess(final String targetProcess) {
        this.targetProcess = targetProcess;
    }

    public void setTargetFlowNode(final String targetFlowNode) {
        this.targetFlowNode = targetFlowNode;
    }

    @Override
    public SEventTriggerType getEventTriggerType() {
        return SEventTriggerType.MESSAGE;
    }

}
