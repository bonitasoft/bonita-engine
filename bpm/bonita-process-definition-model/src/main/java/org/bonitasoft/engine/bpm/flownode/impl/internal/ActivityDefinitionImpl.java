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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import static org.bonitasoft.engine.operation.OperationBuilder.getNonNullCopy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.ObjectSeeker;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.TextDataDefinitionImpl;
import org.bonitasoft.engine.bpm.data.impl.XMLDataDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.impl.OperationImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ActivityDefinitionImpl extends FlowNodeDefinitionImpl implements ActivityDefinition {

    private static final long serialVersionUID = 5575175860474559979L;

    @XmlElementWrapper(name = "dataDefinitions")
    @XmlElements({
            @XmlElement(type = DataDefinitionImpl.class, name = "dataDefinition"),
            @XmlElement(type = TextDataDefinitionImpl.class, name = "textDataDefinition"),
            @XmlElement(type = XMLDataDefinitionImpl.class, name = "xmlDataDefinition")
    })
    private final List<DataDefinition> dataDefinitions = new ArrayList<>();

    @XmlElementWrapper(name = "businessDataDefinitions")
    @XmlElement(type = BusinessDataDefinitionImpl.class, name = "businessDataDefinition")
    private final List<BusinessDataDefinition> businessDataDefinitions = new ArrayList<>(3);

    @XmlElementWrapper(name = "operations")
    @XmlElement(type = OperationImpl.class, name = "operation")
    private final List<Operation> operations = new ArrayList<>();

    @Setter
    @XmlElements({
            @XmlElement(type = StandardLoopCharacteristicsImpl.class, name = "standardLoopCharacteristics"),
            @XmlElement(type = MultiInstanceLoopCharacteristicsImpl.class, name = "multiInstanceLoopCharacteristics")
    })
    private LoopCharacteristics loopCharacteristics;

    @XmlElementWrapper(name = "boundaryEvents")
    @XmlElement(type = BoundaryEventDefinitionImpl.class, name = "boundaryEvent")
    private final List<BoundaryEventDefinition> boundaryEventDefinitions = new ArrayList<>(1);

    protected ActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    protected ActivityDefinitionImpl(final String name) {
        super(name);
    }

    public void addDataDefinition(final DataDefinition dataDefinition) {
        dataDefinitions.add(dataDefinition);
    }

    public void addOperation(final Operation operation) {
        operations.add(getNonNullCopy(operation));
    }

    @Override
    public List<BoundaryEventDefinition> getBoundaryEventDefinitions() {
        return Collections.unmodifiableList(boundaryEventDefinitions);
    }

    public void addBoundaryEventDefinition(final BoundaryEventDefinition boundaryEventDefinition) {
        boundaryEventDefinitions.add(boundaryEventDefinition);
    }

    public void addBusinessDataDefinition(final BusinessDataDefinition businessDataDefinition) {
        businessDataDefinitions.add(businessDataDefinition);
    }

    @Override
    public BusinessDataDefinition getBusinessDataDefinition(final String name) {
        return ObjectSeeker.getNamedElement(businessDataDefinitions, name);
    }

    @Override
    public DataDefinition getDataDefinition(final String name) {
        return ObjectSeeker.getNamedElement(dataDefinitions, name);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
