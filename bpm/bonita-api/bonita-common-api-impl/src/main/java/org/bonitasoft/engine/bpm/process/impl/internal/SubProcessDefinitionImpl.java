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
package org.bonitasoft.engine.bpm.process.impl.internal;

import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SubProcessDefinitionImpl extends ActivityDefinitionImpl implements SubProcessDefinition {

    private static final long serialVersionUID = -5578839351835375715L;
    @XmlAttribute
    private final boolean triggeredByEvent;

    @XmlElement(type = FlowElementContainerDefinitionImpl.class)
    private FlowElementContainerDefinition subProcessContainer;

    public SubProcessDefinitionImpl(final String name, final boolean triggeredByEvent) {
        super(name);
        this.triggeredByEvent = triggeredByEvent;
    }

    public SubProcessDefinitionImpl() {
        triggeredByEvent = false;
    }

    public SubProcessDefinitionImpl(final long id, final String name, final boolean triggeredByEvent) {
        super(id, name);
        this.triggeredByEvent = triggeredByEvent;
    }

    public void setSubProcessContainer(final FlowElementContainerDefinition subProcessContainer) {
        this.subProcessContainer = subProcessContainer;
    }

    @Override
    public FlowElementContainerDefinition getSubProcessContainer() {
        return subProcessContainer;
    }

    @Override
    public boolean isTriggeredByEvent() {
        return triggeredByEvent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (triggeredByEvent ? 1231 : 1237);
        result = prime * result + (subProcessContainer == null ? 0 : subProcessContainer.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubProcessDefinitionImpl other = (SubProcessDefinitionImpl) obj;
        if (triggeredByEvent != other.triggeredByEvent) {
            return false;
        }
        if (subProcessContainer == null) {
            if (other.subProcessContainer != null) {
                return false;
            }
        } else if (!subProcessContainer.equals(other.subProcessContainer)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }

}
