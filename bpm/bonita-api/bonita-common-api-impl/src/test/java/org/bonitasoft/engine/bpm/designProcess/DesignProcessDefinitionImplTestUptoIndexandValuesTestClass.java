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
package org.bonitasoft.engine.bpm.designProcess;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.bpm.process.impl.internal.IndexLabel;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author mazourd
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DesignProcessDefinitionImplTestUptoIndexandValuesTestClass extends ProcessDefinitionImpl implements DesignProcessDefinition, Visitable {

    private static final long serialVersionUID = -4719128363958199300L;
    @XmlAttribute
    private String displayName;
    @XmlAttribute(required = true)
    private String displayDescription;
    @XmlElementWrapper(name = "parameters")
    @XmlElement(type = ParameterDefinitionImpl.class, name = "parameter")
    private final Set<ParameterDefinition> parameters;
    @XmlElementWrapper
    @XmlElement(type = ActorDefinitionImpl.class, name = "actor")
    private final List<ActorDefinition> actors;
    @XmlElement(type = ActorDefinitionImpl.class)
    private ActorDefinition actorInitiator;
    /*
     * @XmlElement(type = FlowElementContainerDefinitionImpl.class)
     * private FlowElementContainerDefinition flowElementContainer;
     */
    @XmlElementWrapper(name = "stringIndexes",required = false,nillable = true)
    @XmlElement(name = "stringIndex")
    private IndexLabel[] listIndex = new IndexLabel[5];
    @XmlElement(type = ContractDefinitionImpl.class)
    private ContractDefinition contract;
    @XmlElementWrapper(name = "contexts")
    @XmlElement(type = ContextEntryImpl.class)
    private List<ContextEntry> context = new ArrayList<>();

    public DesignProcessDefinitionImplTestUptoIndexandValuesTestClass(final String name, final String version) {
        super(name, version);
        parameters = new HashSet<>();
        actors = new ArrayList<>();
        for(int i = 0; i<5;i++){
            listIndex[i] = new IndexLabel(Integer.toString(i+1),"",null);
        }
    }

    public DesignProcessDefinitionImplTestUptoIndexandValuesTestClass() {
        super();
        parameters = new HashSet<>();
        actors = new ArrayList<>();
        for(int i = 0; i<5;i++){
            listIndex[i] = new IndexLabel(Integer.toString(i),"",null);
        }
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
        return null;
    }

    public void setProcessContainer(final FlowElementContainerDefinition processContainer) {

    }

    @Override
    public org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition getFlowElementContainer() {
        return null;
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
                listIndex[0] = new IndexLabel("1",label,initialValue);
                break;
            case 2:
                listIndex[1] = new IndexLabel("2",label,initialValue);
                break;
            case 3:
                listIndex[2] = new IndexLabel("3",label,initialValue);
                break;
            case 4:
               listIndex[3] = new IndexLabel("4",label,initialValue);
                break;
            case 5:
                listIndex[4] = new IndexLabel("5",label,initialValue);
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
        return context;
    }

    public void addContextEntry(ContextEntry contextEntry) {
        context.add(contextEntry);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

    @Override
    public List<String> getStringIndexLabels() {
        return Arrays.asList(getStringIndexLabel(1), getStringIndexLabel(2), getStringIndexLabel(3), getStringIndexLabel(4), getStringIndexLabel(5));
    }

    @Override
    public List<Expression> getStringIndexValues() {
        return Arrays.asList(getStringIndexValue(1), getStringIndexValue(2), getStringIndexValue(3), getStringIndexValue(4), getStringIndexValue(5));
    }


    public void setContext(List<ContextEntry> context) {
        this.context = context;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DesignProcessDefinitionImplTestUptoIndexandValuesTestClass that = (DesignProcessDefinitionImplTestUptoIndexandValuesTestClass) o;
        boolean ArrayCompare = true;
        for (int i = 0; i < 5;i++){
            ArrayCompare = ArrayCompare && listIndex[i].equals(that.listIndex[i]);
        }
        return Objects.equals(displayName, that.displayName) &&
                Objects.equals(displayDescription, that.displayDescription) &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(actors, that.actors) &&
                Objects.equals(actorInitiator, that.actorInitiator) &&
                ArrayCompare &&
                Objects.equals(contract, that.contract) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), displayName, displayDescription, parameters, actors, actorInitiator, listIndex, contract, context);
    }

    @Override
    public String toString() {
        return "DesignProcessDefinitionImplTestUptoIndexandValuesTestClass{" +
                "displayName='" + displayName + '\'' +
                ", displayDescription='" + displayDescription + '\'' +
                ", parameters=" + parameters +
                ", actors=" + actors +
                ", actorInitiator=" + actorInitiator +
                ", listIndex=" + Arrays.toString(listIndex) +
                ", contract=" + contract +
                ", context=" + context +
                '}';
    }
}
