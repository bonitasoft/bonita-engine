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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.SignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

/**
 * @author Matthieu Chaffotte
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class SignalEventTriggerDefinitionImpl implements SignalEventTriggerDefinition {

    private static final long serialVersionUID = 7986619065007971291L;
    @XmlAttribute(name = "name")
    private final String signalName;

    public SignalEventTriggerDefinitionImpl(final String name) {
        this.signalName = name;
    }

    public SignalEventTriggerDefinitionImpl() {
        this.signalName = "default name";
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("signalName", signalName)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignalEventTriggerDefinitionImpl that = (SignalEventTriggerDefinitionImpl) o;
        return Objects.equals(signalName, that.signalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signalName);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        // nothing to do here:
    }
}
