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
package org.bonitasoft.engine.bpm.process.impl.internal;

import java.util.Date;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ArchivedProcessInstanceImpl extends NamedElementImpl implements ArchivedProcessInstance {

    private static final long serialVersionUID = -1924361771241157184L;

    private String state;

    private Date startDate;

    private long startedBy;

    private long startedBySubstitute;

    private Date endDate;

    private Date archiveDate;

    private Date lastUpdate;

    private long sourceObjectId;

    private int stateId;

    private long processDefinitionId;

    private String description;

    private long rootProcessInstanceId;

    private long callerId;

    private String stringIndexValue1;

    private String stringIndexValue2;

    private String stringIndexValue3;

    private String stringIndexValue4;

    private String stringIndexValue5;

    private String stringIndexLabel1;

    private String stringIndexLabel2;

    private String stringIndexLabel3;

    private String stringIndexLabel4;

    private String stringIndexLabel5;

    public ArchivedProcessInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
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

    @Deprecated
    @Override
    public long getStartedByDelegate() {
        return getStartedBySubstitute();
    }

    @Deprecated
    public void setStartedByDelegate(final long startedByDelegate) {
        setStartedBySubstitute(startedByDelegate);
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public int getStateId() {
        return stateId;
    }

    public void setStateId(final int stateId) {
        this.stateId = stateId;
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

    public void setStringIndexValue(final int index, final String value) {
        switch (index) {
            case 1:
                stringIndexValue1 = value;
                break;
            case 2:
                stringIndexValue2 = value;
                break;
            case 3:
                stringIndexValue3 = value;
                break;
            case 4:
                stringIndexValue4 = value;
                break;
            case 5:
                stringIndexValue5 = value;
                break;
            default:
                throw new IndexOutOfBoundsException("string index value must be between 1 and 5 (included)");
        }
    }

    @Override
    public String getStringIndexValue(final int index) {
        switch (index) {
            case 1:
                return stringIndexValue1;
            case 2:
                return stringIndexValue2;
            case 3:
                return stringIndexValue3;
            case 4:
                return stringIndexValue4;
            case 5:
                return stringIndexValue5;
            default:
                throw new IndexOutOfBoundsException("string index value must be between 1 and 5 (included)");
        }
    }

    public void setStringIndexLabel(final int index, final String label) {
        switch (index) {
            case 1:
                stringIndexLabel1 = label;
                break;
            case 2:
                stringIndexLabel2 = label;
                break;
            case 3:
                stringIndexLabel3 = label;
                break;
            case 4:
                stringIndexLabel4 = label;
                break;
            case 5:
                stringIndexLabel5 = label;
                break;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public String getStringIndexLabel(final int index) {
        switch (index) {
            case 1:
                return stringIndexLabel1;
            case 2:
                return stringIndexLabel2;
            case 3:
                return stringIndexLabel3;
            case 4:
                return stringIndexLabel4;
            case 5:
                return stringIndexLabel5;
            default:
                throw new IndexOutOfBoundsException("string index label must be between 1 and 5 (included)");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (archiveDate == null ? 0 : archiveDate.hashCode());
        result = prime * result + (int) (callerId ^ callerId >>> 32);
        result = prime * result + (endDate == null ? 0 : endDate.hashCode());
        result = prime * result + (lastUpdate == null ? 0 : lastUpdate.hashCode());
        result = prime * result + (int) (processDefinitionId ^ processDefinitionId >>> 32);
        result = prime * result + (int) (rootProcessInstanceId ^ rootProcessInstanceId >>> 32);
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        result = prime * result + (startDate == null ? 0 : startDate.hashCode());
        result = prime * result + (int) (startedBy ^ startedBy >>> 32);
        result = prime * result + (int) (startedBySubstitute ^ startedBySubstitute >>> 32);
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + stateId;
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
        final ArchivedProcessInstanceImpl other = (ArchivedProcessInstanceImpl) obj;
        if (archiveDate == null) {
            if (other.archiveDate != null) {
                return false;
            }
        } else if (!archiveDate.equals(other.archiveDate)) {
            return false;
        }
        if (callerId != other.callerId) {
            return false;
        }
        if (endDate == null) {
            if (other.endDate != null) {
                return false;
            }
        } else if (!endDate.equals(other.endDate)) {
            return false;
        }
        if (lastUpdate == null) {
            if (other.lastUpdate != null) {
                return false;
            }
        } else if (!lastUpdate.equals(other.lastUpdate)) {
            return false;
        }
        if (processDefinitionId != other.processDefinitionId) {
            return false;
        }
        if (rootProcessInstanceId != other.rootProcessInstanceId) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (startDate == null) {
            if (other.startDate != null) {
                return false;
            }
        } else if (!startDate.equals(other.startDate)) {
            return false;
        }
        if (startedBy != other.startedBy) {
            return false;
        }
        if (startedBySubstitute != other.startedBySubstitute) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (stateId != other.stateId) {
            return false;
        }
        return true;
    }

}
