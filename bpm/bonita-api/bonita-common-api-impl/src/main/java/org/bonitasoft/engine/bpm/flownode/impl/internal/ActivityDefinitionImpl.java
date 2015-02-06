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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.ObjectSeeker;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class ActivityDefinitionImpl extends FlowNodeDefinitionImpl implements ActivityDefinition {

    private static final long serialVersionUID = 5575175860474559979L;

    private final List<DataDefinition> dataDefinitions;

    private final List<BusinessDataDefinition> businessDataDefinitions;

    private final List<Operation> operations;

    private LoopCharacteristics loopCharacteristics;

    private final List<BoundaryEventDefinition> boundaryEventDefinitions;

    public ActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
        dataDefinitions = new ArrayList<DataDefinition>();
        operations = new ArrayList<Operation>();
        boundaryEventDefinitions = new ArrayList<BoundaryEventDefinition>(1);
        businessDataDefinitions = new ArrayList<BusinessDataDefinition>(3);
    }

    public ActivityDefinitionImpl(final String name) {
        super(name);
        dataDefinitions = new ArrayList<DataDefinition>();
        operations = new ArrayList<Operation>();
        boundaryEventDefinitions = new ArrayList<BoundaryEventDefinition>(1);
        businessDataDefinitions = new ArrayList<BusinessDataDefinition>(3);
    }

    @Override
    public List<DataDefinition> getDataDefinitions() {
        return dataDefinitions;
    }

    public void addDataDefinition(final DataDefinition dataDefinition) {
        dataDefinitions.add(dataDefinition);
    }

    @Override
    public List<Operation> getOperations() {
        return operations;
    }

    public void addOperation(final Operation operation) {
        operations.add(operation);
    }

    @Override
    public List<BoundaryEventDefinition> getBoundaryEventDefinitions() {
        return Collections.unmodifiableList(boundaryEventDefinitions);
    }

    public void addBoundaryEventDefinition(final BoundaryEventDefinition boundaryEventDefinition) {
        boundaryEventDefinitions.add(boundaryEventDefinition);
    }

    @Override
    public LoopCharacteristics getLoopCharacteristics() {
        return loopCharacteristics;
    }

    public void setLoopCharacteristics(final LoopCharacteristics loopCharacteristics) {
        this.loopCharacteristics = loopCharacteristics;
    }

    @Override
    public List<BusinessDataDefinition> getBusinessDataDefinitions() {
        return businessDataDefinitions;
    }

    public void addBusinessDataDefinition(final BusinessDataDefinition businessDataDefinition) {
        businessDataDefinitions.add(businessDataDefinition);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (boundaryEventDefinitions == null ? 0 : boundaryEventDefinitions.hashCode());
        result = prime * result + (businessDataDefinitions == null ? 0 : businessDataDefinitions.hashCode());
        result = prime * result + (dataDefinitions == null ? 0 : dataDefinitions.hashCode());
        result = prime * result + (loopCharacteristics == null ? 0 : loopCharacteristics.hashCode());
        result = prime * result + (operations == null ? 0 : operations.hashCode());
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
        final ActivityDefinitionImpl other = (ActivityDefinitionImpl) obj;
        if (boundaryEventDefinitions == null) {
            if (other.boundaryEventDefinitions != null) {
                return false;
            }
        } else if (!boundaryEventDefinitions.equals(other.boundaryEventDefinitions)) {
            return false;
        }
        if (businessDataDefinitions == null) {
            if (other.businessDataDefinitions != null) {
                return false;
            }
        } else if (!businessDataDefinitions.equals(other.businessDataDefinitions)) {
            return false;
        }
        if (dataDefinitions == null) {
            if (other.dataDefinitions != null) {
                return false;
            }
        } else if (!dataDefinitions.equals(other.dataDefinitions)) {
            return false;
        }
        if (loopCharacteristics == null) {
            if (other.loopCharacteristics != null) {
                return false;
            }
        } else if (!loopCharacteristics.equals(other.loopCharacteristics)) {
            return false;
        }
        if (operations == null) {
            if (other.operations != null) {
                return false;
            }
        } else if (!operations.equals(other.operations)) {
            return false;
        }
        return true;
    }

    @Override
    public BusinessDataDefinition getBusinessDataDefinition(final String name) {
        return ObjectSeeker.getNamedElement(businessDataDefinitions, name);
    }

    @Override
    public DataDefinition getDataDefinition(final String name) {
        return ObjectSeeker.getNamedElement(dataDefinitions, name);
    }

}
