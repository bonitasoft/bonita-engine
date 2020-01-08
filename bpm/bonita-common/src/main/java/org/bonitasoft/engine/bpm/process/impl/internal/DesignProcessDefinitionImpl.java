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
package org.bonitasoft.engine.bpm.process.impl.internal;

import static org.bonitasoft.engine.expression.ExpressionBuilder.getNonNullCopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@XmlRootElement(name = "processDefinition")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ SubProcessDefinitionImpl.class })
public class DesignProcessDefinitionImpl extends ProcessDefinitionImpl implements DesignProcessDefinition, Visitable {

    private static final long serialVersionUID = -4719128363958199300L;
    @XmlAttribute
    private String displayName;
    @XmlElement
    private String displayDescription;
    @XmlElementWrapper(name = "parameters")
    @XmlElement(type = ParameterDefinitionImpl.class, name = "parameter")
    private final Set<ParameterDefinition> parameters;
    @XmlElementWrapper(name = "actors")
    @XmlElement(type = ActorDefinitionImpl.class, name = "actor")
    private final List<ActorDefinition> actors;
    @XmlIDREF
    @XmlElement(type = ActorDefinitionImpl.class)
    private ActorDefinition actorInitiator;
    @XmlElement(type = FlowElementContainerDefinitionImpl.class, name = "flowElements", nillable = true)
    private FlowElementContainerDefinition flowElementContainer;
    @XmlElementWrapper(name = "stringIndexes", required = false, nillable = true)
    @XmlElement(name = "stringIndex")
    private IndexLabel[] listIndex = new IndexLabel[5];
    @XmlElement(type = ContractDefinitionImpl.class)
    private ContractDefinition contract;
    @XmlElementWrapper(name = "context")
    @XmlElement(name = "contextEntry", type = ContextEntryImpl.class, nillable = true)
    private List<ContextEntry> context = new ArrayList<>();

    public DesignProcessDefinitionImpl(final String name, final String version) {
        super(name, version);
        parameters = new HashSet<>();
        actors = new ArrayList<>();
        initStringIndexes();
    }

    void initStringIndexes() {
        for (int i = 0; i < 5; i++) {
            listIndex[i] = new IndexLabel(Integer.toString(i + 1), null, null);
        }
    }

    public DesignProcessDefinitionImpl() {
        super();
        parameters = new HashSet<>();
        actors = new ArrayList<>();
        initStringIndexes();
    }

    public void setDisplayName(final String name) {
        displayName = name;
    }

    public void setDisplayDescription(final String description) {
        displayDescription = description;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    @Override
    public List<ActorDefinition> getActorsList() {
        return actors;
    }

    @Deprecated
    @Override
    public Set<ActorDefinition> getActors() {
        return new HashSet<>(actors);
    }

    @Override
    public Set<ParameterDefinition> getParameters() {
        return parameters;
    }

    public void addParameter(final ParameterDefinition parameter) {
        parameters.add(parameter);
    }

    public void addActor(final ActorDefinition actor) {
        actors.add(actor);
    }

    @Override
    public ActorDefinition getActorInitiator() {
        return actorInitiator;
    }

    public void setActorInitiator(final ActorDefinition actorInitiator) {
        this.actorInitiator = actorInitiator;
    }

    @Override
    public FlowElementContainerDefinition getProcessContainer() {
        return flowElementContainer;
    }

    public void setProcessContainer(final FlowElementContainerDefinition processContainer) {
        flowElementContainer = processContainer;
    }

    @Override
    public org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition getFlowElementContainer() {
        return flowElementContainer;
    }

    @Override
    public String getStringIndexLabel(final int index) {
        switch (index) {
            case 1:
                return listIndex[0].getLabel();
            case 2:
                return listIndex[1].getLabel();
            case 3:
                return listIndex[2].getLabel();
            case 4:
                return listIndex[3].getLabel();
            case 5:
                return listIndex[4].getLabel();
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public Expression getStringIndexValue(final int index) {
        switch (index) {
            case 1:
                return listIndex[0].getValue();
            case 2:
                return listIndex[1].getValue();
            case 3:
                return listIndex[2].getValue();
            case 4:
                return listIndex[3].getValue();
            case 5:
                return listIndex[4].getValue();
            default:
                throw new IndexOutOfBoundsException("string index value must be between 1 and 5 (included)");
        }
    }

    public ActorDefinition getActor(final String actorName) {
        final Iterator<ActorDefinition> iterator = actors.iterator();
        ActorDefinition actorDefinition = null;
        boolean found = false;
        while (!found && iterator.hasNext()) {
            final ActorDefinition next = iterator.next();
            if (next.getName().equals(actorName)) {
                found = true;
                actorDefinition = next;
            }
        }
        return actorDefinition;
    }

    public void setStringIndex(final int index, final String label, final Expression initialValue) {
        switch (index) {
            case 1:
                listIndex[0] = new IndexLabel("1", label, getNonNullCopy(initialValue));
                break;
            case 2:
                listIndex[1] = new IndexLabel("2", label, getNonNullCopy(initialValue));
                break;
            case 3:
                listIndex[2] = new IndexLabel("3", label, getNonNullCopy(initialValue));
                break;
            case 4:
                listIndex[3] = new IndexLabel("4", label, getNonNullCopy(initialValue));
                break;
            case 5:
                listIndex[4] = new IndexLabel("5", label, getNonNullCopy(initialValue));
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public ContractDefinition getContract() {
        return contract;
    }

    public void setContract(ContractDefinition contract) {
        this.contract = contract;
    }

    @Override
    public List<ContextEntry> getContext() {
        if (context == null) {
            return Collections.emptyList();
        }
        return context;
    }

    public void addContextEntry(ContextEntry contextEntry) {
        if (context == null) {
            context = new ArrayList<>();
        }
        context.add(contextEntry);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

    @Override
    public List<String> getStringIndexLabels() {
        return Arrays.asList(getStringIndexLabel(1), getStringIndexLabel(2), getStringIndexLabel(3),
                getStringIndexLabel(4), getStringIndexLabel(5));
    }

    @Override
    public List<Expression> getStringIndexValues() {
        return Arrays.asList(getStringIndexValue(1), getStringIndexValue(2), getStringIndexValue(3),
                getStringIndexValue(4), getStringIndexValue(5));
    }

    /*
     * Bear in mind that you need to modify the automatically generated equals to take into account the listIndex array
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        DesignProcessDefinitionImpl that = (DesignProcessDefinitionImpl) o;
        boolean ArrayCompare = true;
        for (int i = 0; i < 5; i++) {
            ArrayCompare = ArrayCompare && listIndex[i].equals(that.listIndex[i]);
        }
        return Objects.equals(displayName, that.displayName) &&
                Objects.equals(displayDescription, that.displayDescription) &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(actors, that.actors) &&
                Objects.equals(actorInitiator, that.actorInitiator) &&
                Objects.equals(flowElementContainer, that.flowElementContainer) &&
                ArrayCompare &&
                Objects.equals(contract, that.contract) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), displayName, displayDescription, parameters, actors, actorInitiator,
                flowElementContainer, listIndex, contract,
                context);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("displayName", displayName)
                .append("displayDescription", displayDescription)
                .append("parameters", parameters)
                .append("actors", actors)
                .append("actorInitiator", actorInitiator)
                .append("flowElementContainer", flowElementContainer)
                .append("listIndex", listIndex)
                .append("contract", contract)
                .append("context", context)
                .toString();
    }
}
