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
package org.bonitasoft.engine.core.process.instance.model.archive;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("multi")
public class SAMultiInstanceActivityInstance extends SAActivityInstance {

    private boolean sequential;
    private String loopDataInputRef;
    private String loopDataOutputRef;
    private String dataInputItemRef;
    private String dataOutputItemRef;
    @Column(name = "nbActiveInst")
    private int numberOfActiveInstances;
    @Column(name = "nbCompletedInst")
    private int numberOfCompletedInstances;
    @Column(name = "nbTerminatedInst")
    private int numberOfTerminatedInstances;
    private int loopCardinality;

    public SAMultiInstanceActivityInstance(final SMultiInstanceActivityInstance activityInstance) {
        super(activityInstance);
        sequential = activityInstance.isSequential();
        loopDataInputRef = activityInstance.getLoopDataInputRef();
        loopDataOutputRef = activityInstance.getLoopDataOutputRef();
        dataInputItemRef = activityInstance.getDataInputItemRef();
        dataOutputItemRef = activityInstance.getDataOutputItemRef();
        numberOfActiveInstances = activityInstance.getNumberOfActiveInstances();
        numberOfCompletedInstances = activityInstance.getNumberOfCompletedInstances();
        numberOfTerminatedInstances = activityInstance.getNumberOfTerminatedInstances();
        loopCardinality = activityInstance.getLoopCardinality();
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.MULTI_INSTANCE_ACTIVITY;
    }

    public int getNumberOfInstances() {
        return numberOfActiveInstances + numberOfCompletedInstances + numberOfTerminatedInstances;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SMultiInstanceActivityInstance.class;
    }
}
