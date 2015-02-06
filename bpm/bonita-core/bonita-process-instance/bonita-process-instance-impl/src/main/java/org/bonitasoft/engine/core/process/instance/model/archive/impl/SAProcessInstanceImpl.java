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

import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SAProcessInstanceImpl extends SANamedElementImpl implements SAProcessInstance {

    private static final long serialVersionUID = 2195266957946761927L;

    private long processDefinitionId;

    private String description;

    private int stateId;

    private long startDate;

    private long startedBy;

    private long startedBySubstitute;

    private long endDate;

    private long lastUpdate;

    private long rootProcessInstanceId = -1;

    private long callerId = -1;

    private String stringIndex1;

    private String stringIndex2;

    private String stringIndex3;

    private String stringIndex4;

    private String stringIndex5;

    private long migrationPlanId;

    public SAProcessInstanceImpl() {
        super();
    }

    public SAProcessInstanceImpl(final SProcessInstance processInstance) {
        super(processInstance.getName(), processInstance.getId());
        processDefinitionId = processInstance.getProcessDefinitionId();
        description = processInstance.getDescription();
        startDate = processInstance.getStartDate();
        endDate = processInstance.getEndDate();
        startedBy = processInstance.getStartedBy();
        startedBySubstitute = processInstance.getStartedBySubstitute();
        lastUpdate = processInstance.getLastUpdate();
        stateId = processInstance.getStateId();
        rootProcessInstanceId = processInstance.getRootProcessInstanceId();
        callerId = processInstance.getCallerId();
        migrationPlanId = processInstance.getMigrationPlanId();
        stringIndex1 = processInstance.getStringIndex1();
        stringIndex2 = processInstance.getStringIndex2();
        stringIndex3 = processInstance.getStringIndex3();
        stringIndex4 = processInstance.getStringIndex4();
        stringIndex5 = processInstance.getStringIndex5();
    }

    @Override
    public String getDiscriminator() {
        return SAProcessInstance.class.getName();
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public int getStateId() {
        return stateId;
    }

    public void setStateId(final int stateId) {
        this.stateId = stateId;
    }

    @Override
    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(final long startDate) {
        this.startDate = startDate;
    }

    @Override
    public long getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(final long startedBy) {
        this.startedBy = startedBy;
    }

    @Override
    public long getStartedBySubstitute() {
        return startedBySubstitute;
    }

    public void setStartedBySubstitute(final long startedBySubstitute) {
        this.startedBySubstitute = startedBySubstitute;
    }

    @Override
    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(final long endDate) {
        this.endDate = endDate;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public long getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    public void setRootProcessInstanceId(final long rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    @Override
    public long getCallerId() {
        return callerId;
    }

    public void setCallerId(final long callerId) {
        this.callerId = callerId;
    }

    @Override
    public String getStringIndex1() {
        return stringIndex1;
    }

    public void setStringIndex1(final String stringIndex1) {
        this.stringIndex1 = stringIndex1;
    }

    @Override
    public String getStringIndex2() {
        return stringIndex2;
    }

    public void setStringIndex2(final String stringIndex2) {
        this.stringIndex2 = stringIndex2;
    }

    @Override
    public String getStringIndex3() {
        return stringIndex3;
    }

    public void setStringIndex3(final String stringIndex3) {
        this.stringIndex3 = stringIndex3;
    }

    @Override
    public String getStringIndex4() {
        return stringIndex4;
    }

    public void setStringIndex4(final String stringIndex4) {
        this.stringIndex4 = stringIndex4;
    }

    @Override
    public String getStringIndex5() {
        return stringIndex5;
    }

    public void setStringIndex5(final String stringIndex5) {
        this.stringIndex5 = stringIndex5;
    }

    @Override
    public long getMigrationPlanId() {
        return migrationPlanId;
    }

    public void setMigrationPlanId(final long migrationPlanId) {
        this.migrationPlanId = migrationPlanId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (callerId ^ callerId >>> 32);
        result = prime * result + (int) (endDate ^ endDate >>> 32);
        result = prime * result + (int) (lastUpdate ^ lastUpdate >>> 32);
        result = prime * result + (int) (processDefinitionId ^ processDefinitionId >>> 32);
        result = prime * result + (int) (rootProcessInstanceId ^ rootProcessInstanceId >>> 32);
        result = prime * result + (int) (startDate ^ startDate >>> 32);
        result = prime * result + (int) (startedBy ^ startedBy >>> 32);
        result = prime * result + (int) (startedBySubstitute ^ startedBySubstitute >>> 32);
        result = prime * result + stateId;
        result = prime * result + (stringIndex1 == null ? 0 : stringIndex1.hashCode());
        result = prime * result + (stringIndex2 == null ? 0 : stringIndex2.hashCode());
        result = prime * result + (stringIndex3 == null ? 0 : stringIndex3.hashCode());
        result = prime * result + (stringIndex4 == null ? 0 : stringIndex4.hashCode());
        result = prime * result + (stringIndex5 == null ? 0 : stringIndex5.hashCode());
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
        final SAProcessInstanceImpl other = (SAProcessInstanceImpl) obj;
        if (callerId != other.callerId) {
            return false;
        }
        if (endDate != other.endDate) {
            return false;
        }
        if (lastUpdate != other.lastUpdate) {
            return false;
        }
        if (processDefinitionId != other.processDefinitionId) {
            return false;
        }
        if (rootProcessInstanceId != other.rootProcessInstanceId) {
            return false;
        }
        if (startDate != other.startDate) {
            return false;
        }
        if (startedBy != other.startedBy) {
            return false;
        }
        if (startedBySubstitute != other.startedBySubstitute) {
            return false;
        }
        if (stateId != other.stateId) {
            return false;
        }
        if (stringIndex1 == null) {
            if (other.stringIndex1 != null) {
                return false;
            }
        } else if (!stringIndex1.equals(other.stringIndex1)) {
            return false;
        }
        if (stringIndex2 == null) {
            if (other.stringIndex2 != null) {
                return false;
            }
        } else if (!stringIndex2.equals(other.stringIndex2)) {
            return false;
        }
        if (stringIndex3 == null) {
            if (other.stringIndex3 != null) {
                return false;
            }
        } else if (!stringIndex3.equals(other.stringIndex3)) {
            return false;
        }
        if (stringIndex4 == null) {
            if (other.stringIndex4 != null) {
                return false;
            }
        } else if (!stringIndex4.equals(other.stringIndex4)) {
            return false;
        }
        if (stringIndex5 == null) {
            if (other.stringIndex5 != null) {
                return false;
            }
        } else if (!stringIndex5.equals(other.stringIndex5)) {
            return false;
        }
        return true;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SProcessInstance.class;
    }

}
