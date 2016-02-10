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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;

/**
 * FIXME: implement archive version of this class !
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SMultiInstanceActivityInstanceImpl extends SActivityInstanceImpl implements SMultiInstanceActivityInstance {

    private static final long serialVersionUID = -2683110111754584325L;

    private boolean sequential;

    private String loopDataInputRef;

    private String loopDataOutputRef;

    private String dataInputItemRef;

    private String dataOutputItemRef;

    private int numberOfActiveInstances;

    private int numberOfCompletedInstances;

    private int numberOfTerminatedInstances;

    private int loopCardinality;

    public SMultiInstanceActivityInstanceImpl() {
    }

    public SMultiInstanceActivityInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long processDefinitionId, final long rootProcessInstanceId, final boolean isSequential) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, processDefinitionId, rootProcessInstanceId);
        sequential = isSequential;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.MULTI_INSTANCE_ACTIVITY;
    }

    @Override
    public boolean isSequential() {
        return sequential;
    }

    @Override
    public String getLoopDataInputRef() {
        return loopDataInputRef;
    }

    @Override
    public String getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    @Override
    public String getDataInputItemRef() {
        return dataInputItemRef;
    }

    @Override
    public String getDataOutputItemRef() {
        return dataOutputItemRef;
    }

    @Override
    public int getNumberOfInstances() {
        return numberOfActiveInstances + numberOfCompletedInstances + numberOfTerminatedInstances;
    }

    @Override
    public int getNumberOfActiveInstances() {
        return numberOfActiveInstances;
    }

    @Override
    public int getNumberOfCompletedInstances() {
        return numberOfCompletedInstances;
    }

    @Override
    public int getNumberOfTerminatedInstances() {
        return numberOfTerminatedInstances;
    }

    public void setSequential(final boolean sequential) {
        this.sequential = sequential;
    }

    public void setLoopDataInputRef(final String loopDataInputRef) {
        this.loopDataInputRef = loopDataInputRef;
    }

    public void setLoopDataOutputRef(final String loopDataOutputRef) {
        this.loopDataOutputRef = loopDataOutputRef;
    }

    public void setDataInputItemRef(final String dataInputItemRef) {
        this.dataInputItemRef = dataInputItemRef;
    }

    public void setDataOutputItemRef(final String dataOutputItemRef) {
        this.dataOutputItemRef = dataOutputItemRef;
    }

    public void setNumberOfCompletedInstances(final int numberOfCompletedInstances) {
        this.numberOfCompletedInstances = numberOfCompletedInstances;
    }

    public void setNumberOfActiveInstances(final int numberOfActiveInstances) {
        this.numberOfActiveInstances = numberOfActiveInstances;
    }

    public void setNumberOfTerminatedInstances(final int numberOfTerminatedInstances) {
        this.numberOfTerminatedInstances = numberOfTerminatedInstances;
    }

    @Override
    public int getLoopCardinality() {
        return loopCardinality;
    }

    public void setLoopCardinality(final int loopCardinality) {
        this.loopCardinality = loopCardinality;
    }
    
    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        // it's not necessary to execute it because this will be done when the last child reaches the aborted state
        return false;
    }

}
