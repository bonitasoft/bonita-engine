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
import org.bonitasoft.engine.bpm.flownode.CatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CatchEventDefinitionImpl extends EventDefinitionImpl implements CatchEventDefinition {

    private static final long serialVersionUID = 250215494961033080L;
    @XmlElement(type = TimerEventTriggerDefinitionImpl.class, name = "timerEventTrigger")
    private final List<TimerEventTriggerDefinition> timerEventTriggers;
    @XmlElement(type = CatchMessageEventTriggerDefinitionImpl.class, name = "catchMessageEventTrigger")
    private final List<CatchMessageEventTriggerDefinition> messageEventTriggers;
    @XmlElement(type = CatchSignalEventTriggerDefinitionImpl.class, name = "catchSignalEventTrigger")
    private final List<CatchSignalEventTriggerDefinition> signalEventTriggers;
    @XmlElement(type = CatchErrorEventTriggerDefinitionImpl.class, name = "catchErrorEventTrigger")
    private final List<CatchErrorEventTriggerDefinition> errorEventTriggers;
    @XmlAttribute(name = "interrupting")
    private boolean isInterrupting = true;

    public CatchEventDefinitionImpl(final String name) {
        super(name);
        timerEventTriggers = new ArrayList<>(1);
        messageEventTriggers = new ArrayList<>(1);
        signalEventTriggers = new ArrayList<>(1);
        errorEventTriggers = new ArrayList<>(1);
    }

    public CatchEventDefinitionImpl(final long id, final String name) {
        super(id, name);
        timerEventTriggers = new ArrayList<>(1);
        messageEventTriggers = new ArrayList<>(1);
        signalEventTriggers = new ArrayList<>(1);
        errorEventTriggers = new ArrayList<>(1);
    }

    public CatchEventDefinitionImpl() {
        super();
        timerEventTriggers = new ArrayList<>(1);
        messageEventTriggers = new ArrayList<>(1);
        signalEventTriggers = new ArrayList<>(1);
        errorEventTriggers = new ArrayList<>(1);
    }

    @Override
    public List<TimerEventTriggerDefinition> getTimerEventTriggerDefinitions() {
        return Collections.unmodifiableList(timerEventTriggers);
    }

    public void addTimerEventTrigger(final TimerEventTriggerDefinition timerEventDefinition) {
        timerEventTriggers.add(timerEventDefinition);
        addEventTrigger(timerEventDefinition);
    }

    @Override
    public List<CatchMessageEventTriggerDefinition> getMessageEventTriggerDefinitions() {
        return Collections.unmodifiableList(messageEventTriggers);
    }

    public void addMessageEventTrigger(final CatchMessageEventTriggerDefinition messageEventTrigger) {
        messageEventTriggers.add(messageEventTrigger);
        addEventTrigger(messageEventTrigger);
    }

    @Override
    public List<CatchSignalEventTriggerDefinition> getSignalEventTriggerDefinitions() {
        return Collections.unmodifiableList(signalEventTriggers);
    }

    public void addSignalEventTrigger(final CatchSignalEventTriggerDefinition signalEventTrigger) {
        signalEventTriggers.add(signalEventTrigger);
        addEventTrigger(signalEventTrigger);
    }

    public void addErrorEventTrigger(final CatchErrorEventTriggerDefinition errorEventTrigger) {
        errorEventTriggers.add(errorEventTrigger);
        addEventTrigger(errorEventTrigger);
    }

    @Override
    public List<CatchErrorEventTriggerDefinition> getErrorEventTriggerDefinitions() {
        return Collections.unmodifiableList(errorEventTriggers);
    }

    public void setInterrupting(final boolean isInterrupting) {
        this.isInterrupting = isInterrupting;
    }

    @Override
    public boolean isInterrupting() {
        return isInterrupting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CatchEventDefinitionImpl that = (CatchEventDefinitionImpl) o;
        return Objects.equals(isInterrupting, that.isInterrupting) &&
                Objects.equals(timerEventTriggers, that.timerEventTriggers) &&
                Objects.equals(messageEventTriggers, that.messageEventTriggers) &&
                Objects.equals(signalEventTriggers, that.signalEventTriggers) &&
                Objects.equals(errorEventTriggers, that.errorEventTriggers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timerEventTriggers, messageEventTriggers, signalEventTriggers, errorEventTriggers, isInterrupting);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timerEventTriggers", timerEventTriggers)
                .append("messageEventTriggers", messageEventTriggers)
                .append("signalEventTriggers", signalEventTriggers)
                .append("errorEventTriggers", errorEventTriggers)
                .append("isInterrupting", isInterrupting)
                .toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
