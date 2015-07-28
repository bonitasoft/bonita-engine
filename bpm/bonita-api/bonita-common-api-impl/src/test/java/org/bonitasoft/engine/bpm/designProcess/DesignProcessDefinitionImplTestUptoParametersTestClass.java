/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.bpm.designProcess;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author mazourd
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DesignProcessDefinitionImplTestUptoParametersTestClass extends ProcessDefinitionImpl implements DesignProcessDefinition, Visitable{
    private static final long serialVersionUID = -4719128363958199300L;
    @XmlAttribute
    private String displayName;
    @XmlAttribute(required = true)
    private String displayDescription;
    @XmlElementWrapper(name="parameters")
    @XmlElement(type = ParameterDefinitionImpl.class,name = "parameter")
    private final Set<ParameterDefinition> parameters;
    /*@XmlElementWrapper
    @XmlElement(type = ActorDefinitionImpl.class)
    private final List<ActorDefinition> actors;
    @XmlElement(type = ActorDefinitionImpl.class)
    private ActorDefinition actorInitiator;
    @XmlElement(type = FlowElementContainerDefinitionImpl.class)
    private FlowElementContainerDefinition flowElementContainer;
    @XmlAttribute
    private String stringIndexLabel1;
    @XmlAttribute
    private String stringIndexLabel2;
    @XmlAttribute
    private String stringIndexLabel3;
    @XmlAttribute
    private String stringIndexLabel4;
    @XmlAttribute
    private String stringIndexLabel5;
    @XmlElement(type = ExpressionImpl.class)
    private Expression stringIndexValue1;
    @XmlElement(type = ExpressionImpl.class)
    private Expression stringIndexValue2;
    @XmlElement(type = ExpressionImpl.class)
    private Expression stringIndexValue3;
    @XmlElement(type = ExpressionImpl.class)
    private Expression stringIndexValue4;
    @XmlElement(type = ExpressionImpl.class)
    private Expression stringIndexValue5;
    @XmlElement(type = ContractDefinitionImpl.class)
    private ContractDefinition contract;
    @XmlElementWrapper(name = "context")
    @XmlElement(type = ContextEntryImpl.class)
    private List<ContextEntry> context = new ArrayList<>();
*/
    public DesignProcessDefinitionImplTestUptoParametersTestClass(final String name, final String version) {
        super(name, version);
        parameters = new HashSet<>();
    }
    public DesignProcessDefinitionImplTestUptoParametersTestClass(){
        super();
        parameters = new HashSet<>();
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
    public FlowElementContainerDefinition getProcessContainer() {
        return null;
    }

    @Override
    public org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition getFlowElementContainer() {
        return null;
    }

    @Override
    public Set<ParameterDefinition> getParameters() {
        return parameters;
    }

    @Override
    public Set<ActorDefinition> getActors() {
        return null;
    }

    @Override
    public List<ActorDefinition> getActorsList() {
        return null;
    }

    @Override
    public ActorDefinition getActorInitiator() {
        return null;
    }

    @Override
    public String getStringIndexLabel(int index) {
        return null;
    }

    public void addParameter(final ParameterDefinition parameter) {
        parameters.add(parameter);
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
    public Expression getStringIndexValue(int index) {
        return null;
    }

    @Override
    public List<Expression> getStringIndexValues() {
        return Arrays.asList(getStringIndexValue(1), getStringIndexValue(2), getStringIndexValue(3), getStringIndexValue(4), getStringIndexValue(5));
    }

    @Override
    public ContractDefinition getContract() {
        return null;
    }

    @Override
    public List<ContextEntry> getContext() {
        return null;
    }

    @Override
    public String toString() {
        return "DesignProcessDefinitionImplTestUptoParametersTestClass{" +
                "displayName='" + displayName + '\'' +
                ", displayDescription='" + displayDescription + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DesignProcessDefinitionImplTestUptoParametersTestClass that = (DesignProcessDefinitionImplTestUptoParametersTestClass) o;
        return Objects.equals(displayName, that.displayName) &&
                Objects.equals(displayDescription, that.displayDescription) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), displayName, displayDescription, parameters);
    }
}

