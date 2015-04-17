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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class DesignProcessDefinitionImpl extends ProcessDefinitionImpl implements DesignProcessDefinition {

    private static final long serialVersionUID = -4719128363958199300L;

    private String displayName;

    private String displayDescription;

    private final Set<ParameterDefinition> parameters;

    private final List<ActorDefinition> actors;

    private ActorDefinition actorInitiator;

    private FlowElementContainerDefinition flowElementContainer;

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    private Expression stringIndexValue1;

    private Expression stringIndexValue2;

    private Expression stringIndexValue3;

    private Expression stringIndexValue4;

    private Expression stringIndexValue5;

    private ContractDefinition contract;

    private List<ContextEntry> context = new ArrayList<>();

    public DesignProcessDefinitionImpl(final String name, final String version) {
        super(name, version);
        parameters = new HashSet<ParameterDefinition>();
        actors = new ArrayList<ActorDefinition>();
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
        return new HashSet<ActorDefinition>(actors);
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
                return stringIndexLabel1;
            case 2:
                return stringIndexLabel2;
            case 3:
                return stringIndexLabel3;
            case 4:
                return stringIndexLabel4;
            case 5:
                return stringIndexLabel5;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public Expression getStringIndexValue(final int index) {
        switch (index) {
            case 1:
                return stringIndexValue1;
            case 2:
                return stringIndexValue2;
            case 3:
                return stringIndexValue3;
            case 4:
                return stringIndexValue4;
            case 5:
                return stringIndexValue5;
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
                stringIndexLabel1 = label;
                stringIndexValue1 = initialValue;
                break;
            case 2:
                stringIndexLabel2 = label;
                stringIndexValue2 = initialValue;
                break;
            case 3:
                stringIndexLabel3 = label;
                stringIndexValue3 = initialValue;
                break;
            case 4:
                stringIndexLabel4 = label;
                stringIndexValue4 = initialValue;
                break;
            case 5:
                stringIndexLabel5 = label;
                stringIndexValue5 = initialValue;
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(displayName).append(displayDescription).append(parameters).append(actors).append(actorInitiator)
                .append(flowElementContainer).append(stringIndexLabel1).append(stringIndexLabel2).append(stringIndexLabel3).append(stringIndexLabel4)
                .append(stringIndexLabel5).append(stringIndexValue1).append(stringIndexValue2).append(stringIndexValue3).append(stringIndexValue4)
                .append(stringIndexValue5).append(contract).build();
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
        final DesignProcessDefinitionImpl other = (DesignProcessDefinitionImpl) obj;
        return new EqualsBuilder().append(displayName, other.displayName).append(displayDescription, other.displayDescription)
                .append(parameters, other.parameters).append(actors, other.actors).append(actorInitiator, other.actorInitiator)
                .append(flowElementContainer, other.flowElementContainer).append(stringIndexLabel1, other.stringIndexLabel1)
                .append(stringIndexLabel2, other.stringIndexLabel2).append(stringIndexLabel3, other.stringIndexLabel3)
                .append(stringIndexLabel4, other.stringIndexLabel4).append(stringIndexLabel5, other.stringIndexLabel5)
                .append(stringIndexValue1, other.stringIndexValue1).append(stringIndexValue2, other.stringIndexValue2)
                .append(stringIndexValue3, other.stringIndexValue3).append(stringIndexValue4, other.stringIndexValue4)
                .append(stringIndexValue5, other.stringIndexValue5).append(contract, other.contract).build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(getName()).append(getVersion()).append(displayName)
                .append(displayDescription).append(parameters).append(actors).append(actorInitiator).append(flowElementContainer).append(stringIndexLabel1)
                .append(stringIndexLabel2).append(stringIndexLabel3).append(stringIndexLabel4).append(stringIndexLabel5).append(stringIndexValue1)
                .append(stringIndexValue2).append(stringIndexValue3).append(stringIndexValue4).append(stringIndexValue5).append(contract).build();
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
        return context;
    }

    public void addContextEntry(ContextEntry contextEntry) {
        context.add(contextEntry);
    }
}
