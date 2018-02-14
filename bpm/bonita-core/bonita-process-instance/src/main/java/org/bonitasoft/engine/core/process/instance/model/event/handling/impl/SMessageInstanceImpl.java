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

import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SPersistenceObjectImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SMessageInstanceImpl extends SPersistenceObjectImpl implements SMessageInstance {

    private static final long serialVersionUID = -5836326728525413020L;

    private String messageName;

    private String targetProcess;

    private String targetFlowNode;

    private long processDefinitionId;

    private boolean locked = false;

    private boolean handled = false;

    private String flowNodeName;

    private String correlation1;

    private String correlation2;

    private String correlation3;

    private String correlation4;

    private String correlation5;

    public SMessageInstanceImpl() {
    }

    public SMessageInstanceImpl(final SThrowMessageEventTriggerInstance throwMessageTrigger, final long processDefinitionId, final String flowNodeName) {
        messageName = throwMessageTrigger.getMessageName();
        targetProcess = throwMessageTrigger.getTargetProcess();
        targetFlowNode = throwMessageTrigger.getTargetFlowNode();
        this.processDefinitionId = processDefinitionId;
        this.flowNodeName = flowNodeName;
    }

    public SMessageInstanceImpl(final String messageName, final String targetProcess, final String targetFlowNode, final long processDefinitionId,
            final String flowNodeName) {
        this.messageName = messageName;
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        this.processDefinitionId = processDefinitionId;
        this.flowNodeName = flowNodeName;
    }

    @Override
    public String getDiscriminator() {
        return SMessageInstance.class.getName();
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
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isHandled() {
        return handled;
    }

    public void setHandled(final boolean handled) {
        this.handled = handled;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public String getFlowNodeName() {
        return flowNodeName;
    }

    public void setFlowNodeName(final String flowNodeName) {
        this.flowNodeName = flowNodeName;
    }

    /**
     * @return the correlation1
     */
    @Override
    public String getCorrelation1() {
        return correlation1;
    }

    /**
     * @param correlation1
     *            the correlation1 to set
     */
    public void setCorrelation1(final String correlation1) {
        this.correlation1 = correlation1;
    }

    /**
     * @return the correlation2
     */
    @Override
    public String getCorrelation2() {
        return correlation2;
    }

    /**
     * @param correlation2
     *            the correlation2 to set
     */
    public void setCorrelation2(final String correlation2) {
        this.correlation2 = correlation2;
    }

    /**
     * @return the correlation3
     */
    @Override
    public String getCorrelation3() {
        return correlation3;
    }

    /**
     * @param correlation3
     *            the correlation3 to set
     */
    public void setCorrelation3(final String correlation3) {
        this.correlation3 = correlation3;
    }

    /**
     * @return the correlation4
     */
    @Override
    public String getCorrelation4() {
        return correlation4;
    }

    /**
     * @param correlation4
     *            the correlation4 to set
     */
    public void setCorrelation4(final String correlation4) {
        this.correlation4 = correlation4;
    }

    /**
     * @return the correlation5
     */
    @Override
    public String getCorrelation5() {
        return correlation5;
    }

    /**
     * @param correlation5
     *            the correlation5 to set
     */
    public void setCorrelation5(final String correlation5) {
        this.correlation5 = correlation5;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (correlation1 == null ? 0 : correlation1.hashCode());
        result = prime * result + (correlation2 == null ? 0 : correlation2.hashCode());
        result = prime * result + (correlation3 == null ? 0 : correlation3.hashCode());
        result = prime * result + (correlation4 == null ? 0 : correlation4.hashCode());
        result = prime * result + (correlation5 == null ? 0 : correlation5.hashCode());
        result = prime * result + (flowNodeName == null ? 0 : flowNodeName.hashCode());
        result = prime * result + (handled ? 1231 : 1237);
        result = prime * result + (locked ? 1231 : 1237);
        result = prime * result + (messageName == null ? 0 : messageName.hashCode());
        result = prime * result + (int) (processDefinitionId ^ processDefinitionId >>> 32);
        result = prime * result + (targetFlowNode == null ? 0 : targetFlowNode.hashCode());
        result = prime * result + (targetProcess == null ? 0 : targetProcess.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SMessageInstanceImpl other = (SMessageInstanceImpl) obj;
        if (correlation1 == null) {
            if (other.correlation1 != null) {
                return false;
            }
        } else if (!correlation1.equals(other.correlation1)) {
            return false;
        }
        if (correlation2 == null) {
            if (other.correlation2 != null) {
                return false;
            }
        } else if (!correlation2.equals(other.correlation2)) {
            return false;
        }
        if (correlation3 == null) {
            if (other.correlation3 != null) {
                return false;
            }
        } else if (!correlation3.equals(other.correlation3)) {
            return false;
        }
        if (correlation4 == null) {
            if (other.correlation4 != null) {
                return false;
            }
        } else if (!correlation4.equals(other.correlation4)) {
            return false;
        }
        if (correlation5 == null) {
            if (other.correlation5 != null) {
                return false;
            }
        } else if (!correlation5.equals(other.correlation5)) {
            return false;
        }
        if (flowNodeName == null) {
            if (other.flowNodeName != null) {
                return false;
            }
        } else if (!flowNodeName.equals(other.flowNodeName)) {
            return false;
        }
        if (handled != other.handled) {
            return false;
        }
        if (locked != other.locked) {
            return false;
        }
        if (messageName == null) {
            if (other.messageName != null) {
                return false;
            }
        } else if (!messageName.equals(other.messageName)) {
            return false;
        }
        if (processDefinitionId != other.processDefinitionId) {
            return false;
        }
        if (targetFlowNode == null) {
            if (other.targetFlowNode != null) {
                return false;
            }
        } else if (!targetFlowNode.equals(other.targetFlowNode)) {
            return false;
        }
        if (targetProcess == null) {
            if (other.targetProcess != null) {
                return false;
            }
        } else if (!targetProcess.equals(other.targetProcess)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SMessageInstanceImpl [messageName=" + messageName + ", targetProcess=" + targetProcess + ", targetFlowNode=" + targetFlowNode
                + ", processDefinitionId=" + processDefinitionId + ", locked=" + locked + ", handled=" + handled + ", flowNodeName=" + flowNodeName
                + ", correlation1=" + correlation1 + ", correlation2=" + correlation2 + ", correlation3=" + correlation3 + ", correlation4=" + correlation4
                + ", correlation5=" + correlation5 + "]";
    }

}
