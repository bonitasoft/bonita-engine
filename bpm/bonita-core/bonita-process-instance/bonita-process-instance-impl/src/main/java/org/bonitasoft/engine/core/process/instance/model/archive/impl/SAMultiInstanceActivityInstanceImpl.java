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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAMultiInstanceActivityInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SAMultiInstanceActivityInstanceImpl extends SAActivityInstanceImpl implements SAMultiInstanceActivityInstance {

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

    public SAMultiInstanceActivityInstanceImpl() {
    }

    public SAMultiInstanceActivityInstanceImpl(final SMultiInstanceActivityInstance activityInstance) {
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

    public void setNumberOfActiveInstances(final int numberOfActiveInstances) {
        this.numberOfActiveInstances = numberOfActiveInstances;
    }

    public void setNumberOfCompletedInstances(final int numberOfCompletedInstances) {
        this.numberOfCompletedInstances = numberOfCompletedInstances;
    }

    public void setNumberOfTerminatedInstances(final int numberOfTerminatedInstances) {
        this.numberOfTerminatedInstances = numberOfTerminatedInstances;
    }

    public void setLoopCardinality(final int loopCardinality) {
        this.loopCardinality = loopCardinality;
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

    @Override
    public int getLoopCardinality() {
        return loopCardinality;
    }

    @Override
    public String getKind() {
        return "multi";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SMultiInstanceActivityInstance.class;
    }

    @Override
    public String getDiscriminator() {
        return SAMultiInstanceActivityInstance.class.getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (dataInputItemRef == null ? 0 : dataInputItemRef.hashCode());
        result = prime * result + (dataOutputItemRef == null ? 0 : dataOutputItemRef.hashCode());
        result = prime * result + loopCardinality;
        result = prime * result + (loopDataInputRef == null ? 0 : loopDataInputRef.hashCode());
        result = prime * result + (loopDataOutputRef == null ? 0 : loopDataOutputRef.hashCode());
        result = prime * result + numberOfActiveInstances;
        result = prime * result + numberOfCompletedInstances;
        result = prime * result + numberOfTerminatedInstances;
        result = prime * result + (sequential ? 1231 : 1237);
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
        final SAMultiInstanceActivityInstanceImpl other = (SAMultiInstanceActivityInstanceImpl) obj;
        if (dataInputItemRef == null) {
            if (other.dataInputItemRef != null) {
                return false;
            }
        } else if (!dataInputItemRef.equals(other.dataInputItemRef)) {
            return false;
        }
        if (dataOutputItemRef == null) {
            if (other.dataOutputItemRef != null) {
                return false;
            }
        } else if (!dataOutputItemRef.equals(other.dataOutputItemRef)) {
            return false;
        }
        if (loopCardinality != other.loopCardinality) {
            return false;
        }
        if (loopDataInputRef == null) {
            if (other.loopDataInputRef != null) {
                return false;
            }
        } else if (!loopDataInputRef.equals(other.loopDataInputRef)) {
            return false;
        }
        if (loopDataOutputRef == null) {
            if (other.loopDataOutputRef != null) {
                return false;
            }
        } else if (!loopDataOutputRef.equals(other.loopDataOutputRef)) {
            return false;
        }
        if (numberOfActiveInstances != other.numberOfActiveInstances) {
            return false;
        }
        if (numberOfCompletedInstances != other.numberOfCompletedInstances) {
            return false;
        }
        if (numberOfTerminatedInstances != other.numberOfTerminatedInstances) {
            return false;
        }
        if (sequential != other.sequential) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SAMultiInstanceActivityInstanceImpl [sequential=" + sequential + ", loopDataInputRef=" + loopDataInputRef + ", loopDataOutputRef="
                + loopDataOutputRef + ", dataInputItemRef=" + dataInputItemRef + ", dataOutputItemRef=" + dataOutputItemRef + ", numberOfActiveInstances="
                + numberOfActiveInstances + ", numberOfCompletedInstances=" + numberOfCompletedInstances + ", numberOfTerminatedInstances="
                + numberOfTerminatedInstances + ", loopCardinality=" + loopCardinality + ", isAborting()=" + isAborting() + ", getName()=" + getName()
                + ", getTenantId()=" + getTenantId() + ", getId()=" + getId() + ", getArchiveDate()=" + getArchiveDate() + ", getSourceObjectId()="
                + getSourceObjectId() + "]";
    }

}
