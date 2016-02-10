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

import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceActivityInstance;

/**
 * @author Baptiste Mesta
 */
public class MultiInstanceActivityInstanceImpl extends ActivityInstanceImpl implements MultiInstanceActivityInstance {

    private static final long serialVersionUID = -5723988334123367841L;

    private final boolean sequential;

    private final String loopDataInputRef;

    private final String loopDataOutputRef;

    private final String dataInputItemRef;

    private final String dataOutputItemRef;

    private final int numberOfActiveInstances;

    private final int numberOfCompletedInstances;

    private final int numberOfTerminatedInstances;

    private final int loopCardinality;

    public MultiInstanceActivityInstanceImpl(final String name, final long flownodeDefinitionId, final boolean sequential, final String loopDataInputRef,
            final String loopDataOutputRef, final String dataInputItemRef, final String dataOutputItemRef, final int numberOfActiveInstances,
            final int numberOfCompletedInstances, final int numberOfTerminatedInstances, final int loopCardinality) {
        super(name, flownodeDefinitionId);
        this.sequential = sequential;
        this.loopDataInputRef = loopDataInputRef;
        this.loopDataOutputRef = loopDataOutputRef;
        this.dataInputItemRef = dataInputItemRef;
        this.dataOutputItemRef = dataOutputItemRef;
        this.numberOfActiveInstances = numberOfActiveInstances;
        this.numberOfCompletedInstances = numberOfCompletedInstances;
        this.numberOfTerminatedInstances = numberOfTerminatedInstances;
        this.loopCardinality = loopCardinality;
    }

    /**
     * @return the sequential
     */
    @Override
    public boolean isSequential() {
        return sequential;
    }

    /**
     * @return the loopDataInputRef
     */
    @Override
    public String getLoopDataInputRef() {
        return loopDataInputRef;
    }

    /**
     * @return the loopDataOutputRef
     */
    @Override
    public String getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    /**
     * @return the dataInputItemRef
     */
    @Override
    public String getDataInputItemRef() {
        return dataInputItemRef;
    }

    /**
     * @return the dataOutputItemRef
     */
    @Override
    public String getDataOutputItemRef() {
        return dataOutputItemRef;
    }

    /**
     * @return the numberOfActiveInstances
     */
    @Override
    public int getNumberOfActiveInstances() {
        return numberOfActiveInstances;
    }

    /**
     * @return the numberOfCompletedInstances
     */
    @Override
    public int getNumberOfCompletedInstances() {
        return numberOfCompletedInstances;
    }

    /**
     * @return the numberOfTerminatedInstances
     */
    @Override
    public int getNumberOfTerminatedInstances() {
        return numberOfTerminatedInstances;
    }

    /**
     * @return the loopCardinality
     */
    @Override
    public int getLoopCardinality() {
        return loopCardinality;
    }

    @Override
    public FlowNodeType getType() {
        return FlowNodeType.MULTI_INSTANCE_ACTIVITY;
    }

    @Override
    public int getNumberOfInstances() {
        return numberOfActiveInstances + numberOfCompletedInstances + numberOfTerminatedInstances;
    }

}
