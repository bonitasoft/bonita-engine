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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristics;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBoundaryEventNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SBoundaryEventDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public abstract class SActivityDefinitionImpl extends SFlowNodeDefinitionImpl implements SActivityDefinition {

    private static final long serialVersionUID = 8767258220640127769L;

    protected List<SDataDefinition> sDataDefinitions = new ArrayList<SDataDefinition>();

    private final List<SBusinessDataDefinition> businessDataDefinitions = new ArrayList<SBusinessDataDefinition>();

    protected List<SOperation> sOperations = new ArrayList<SOperation>();

    protected SLoopCharacteristics loopCharacteristics;

    private final List<SBoundaryEventDefinition> sBoundaryEventDefinitions = new ArrayList<SBoundaryEventDefinition>();

    public SActivityDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    public SActivityDefinitionImpl(final ActivityDefinition activityDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(activityDefinition, transitionsMap);

        final List<DataDefinition> dataDefinitions = activityDefinition.getDataDefinitions();
        for (final DataDefinition dataDefinition : dataDefinitions) {
            sDataDefinitions.add(ServerModelConvertor.convertDataDefinition(dataDefinition));
        }
        for (final BusinessDataDefinition businessDataDefinition : activityDefinition.getBusinessDataDefinitions()) {
            businessDataDefinitions.add(ServerModelConvertor.convertBusinessDataDefinition(businessDataDefinition));
        }
        final List<Operation> operations = activityDefinition.getOperations();
        for (final Operation operation : operations) {
            sOperations.add(ServerModelConvertor.convertOperation(operation));
        }
        final LoopCharacteristics loop = activityDefinition.getLoopCharacteristics();
        if (loop != null) {
            if (loop instanceof StandardLoopCharacteristics) {
                loopCharacteristics = new SStandardLoopCharacteristicsImpl((StandardLoopCharacteristics) loop);
            } else {
                loopCharacteristics = new SMultiInstanceLoopCharacteristicsImpl((MultiInstanceLoopCharacteristics) loop);
            }
        }
        addBoundaryEvents(activityDefinition, transitionsMap);
    }

    private void addBoundaryEvents(final ActivityDefinition activityDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        final List<BoundaryEventDefinition> boundaryEventDefinitions = activityDefinition.getBoundaryEventDefinitions();
        for (final BoundaryEventDefinition boundaryEventDefinition : boundaryEventDefinitions) {
            addBoundaryEventDefinition(new SBoundaryEventDefinitionImpl(boundaryEventDefinition, transitionsMap));
        }
    }

    @Override
    public List<SOperation> getSOperations() {
        return sOperations;
    }

    public void addSOperation(final SOperation operation) {
        sOperations.add(operation);
    }

    @Override
    public List<SDataDefinition> getSDataDefinitions() {
        return sDataDefinitions;
    }

    public void addSDataDefinition(final SDataDefinition sDataDefinition) {
        sDataDefinitions.add(sDataDefinition);
    }

    @Override
    public List<SBoundaryEventDefinition> getBoundaryEventDefinitions() {
        return Collections.unmodifiableList(sBoundaryEventDefinitions);
    }

    @Override
    public SBoundaryEventDefinition getBoundaryEventDefinition(final String name) throws SBoundaryEventNotFoundException {
        boolean found = false;
        SBoundaryEventDefinition boundary = null;
        final Iterator<SBoundaryEventDefinition> iterator = sBoundaryEventDefinitions.iterator();
        while (iterator.hasNext() && !found) {
            final SBoundaryEventDefinition currentBoundary = iterator.next();
            if (currentBoundary.getName().equals(name)) {
                boundary = currentBoundary;
                found = true;
            }
        }
        if (boundary == null) {
            throw new SBoundaryEventNotFoundException(name, getName());
        }
        return boundary;
    }

    public void addBoundaryEventDefinition(final SBoundaryEventDefinition boundaryEventDefinition) {
        sBoundaryEventDefinitions.add(boundaryEventDefinition);
    }

    @Override
    public SLoopCharacteristics getLoopCharacteristics() {
        return loopCharacteristics;
    }

    public void setLoopCharacteristics(final SLoopCharacteristics loopCharacteristics) {
        this.loopCharacteristics = loopCharacteristics;
    }

    @Override
    public List<SBusinessDataDefinition> getBusinessDataDefinitions() {
        return businessDataDefinitions;
    }

    public void addBusinessDataDefinition(final SBusinessDataDefinition businessDataDefinition) {
        businessDataDefinitions.add(businessDataDefinition);
    }

    @Override
    public SBusinessDataDefinition getBusinessDataDefinition(final String name) {
        if (name == null) {
            return null;
        }
        boolean found = false;
        SBusinessDataDefinition businessData = null;
        final Iterator<SBusinessDataDefinition> iterator = businessDataDefinitions.iterator();
        while (iterator.hasNext() && !found) {
            final SBusinessDataDefinition currentBusinessData = iterator.next();
            if (currentBusinessData.getName().equals(name)) {
                found = true;
                businessData = currentBusinessData;
            }
        }
        return businessData;
    }

}
