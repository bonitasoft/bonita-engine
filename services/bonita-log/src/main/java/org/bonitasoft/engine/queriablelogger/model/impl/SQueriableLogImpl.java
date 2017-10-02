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
package org.bonitasoft.engine.queriablelogger.model.impl;

import java.util.Calendar;
import java.util.Date;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class SQueriableLogImpl implements SQueriableLog {

    private static final long serialVersionUID = -1680378231407104908L;

    private long tenantId;

    private long id;

    private long timeStamp;

    private int year;

    private int month;

    private int dayOfYear;

    private int weekOfYear;

    private String userId;

    private final long threadNumber;

    private String clusterNode;

    private String productVersion;

    private SQueriableLogSeverity severity;

    private String actionType;

    private String actionScope;

    private int actionStatus;

    private String rawMessage;

    private String callerClassName;

    private String callerMethodName;

    private long numericIndex1;

    private long numericIndex2;

    private long numericIndex3;

    private long numericIndex4;

    private long numericIndex5;

    public SQueriableLogImpl() {
        numericIndex1 = -1;
        numericIndex2 = -1;
        numericIndex3 = -1;
        numericIndex4 = -1;
        numericIndex5 = -1;

        threadNumber = Thread.currentThread().getId();
        severity = null;
        actionStatus = -1;

        final Date date = new Date();
        updateDateElements(date);
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return SQueriableLog.class.getName();
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getDayOfYear() {
        return dayOfYear;
    }

    @Override
    public int getWeekOfYear() {
        return weekOfYear;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public long getThreadNumber() {
        return threadNumber;
    }

    @Override
    public String getClusterNode() {
        return clusterNode;
    }

    @Override
    public String getProductVersion() {
        return productVersion;
    }

    @Override
    public SQueriableLogSeverity getSeverity() {
        return severity;
    }

    @Override
    public String getActionType() {
        return actionType;
    }

    @Override
    public String getActionScope() {
        return actionScope;
    }

    @Override
    public int getActionStatus() {
        return actionStatus;
    }

    @Override
    public String getRawMessage() {
        return rawMessage;
    }

    @Override
    public String getCallerClassName() {
        return callerClassName;
    }

    @Override
    public String getCallerMethodName() {
        return callerMethodName;
    }

    @Override
    public long getNumericIndex(final int pos) {
        long result = -1;
        switch (pos) {
            case 0:
                result = numericIndex1;
                break;
            case 1:
                result = numericIndex2;
                break;
            case 2:
                result = numericIndex3;
                break;
            case 3:
                result = numericIndex4;
                break;
            case 4:
                result = numericIndex5;
                break;
            default:
                throw new IllegalStateException();
        }
        return result;
    }

    private void updateDateElements(final Date date) {
        timeStamp = date.getTime();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1; // January is 0

        weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
    }

    public void setClusterNode(final String clusterNode) {
        this.clusterNode = clusterNode;
    }

    public void setProductVersion(final String productVersion) {
        this.productVersion = productVersion;
    }

    public void setSeverity(final SQueriableLogSeverity severity) {
        this.severity = severity;
    }

    public void setActionType(final String actionType) {
        this.actionType = actionType;
    }

    public void setActionScope(final String actionScope) {
        this.actionScope = actionScope;
    }

    public void setActionStatus(final int actionStatus) {
        this.actionStatus = actionStatus;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void setRawMessage(final String rawMessage) {
        this.rawMessage = rawMessage;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    public void setNumericIndex(final int pos, final long value) {
        switch (pos) {
            case 0:
                numericIndex1 = value;
                break;
            case 1:
                numericIndex2 = value;
                break;
            case 2:
                numericIndex3 = value;
                break;
            case 3:
                numericIndex4 = value;
                break;
            case 4:
                numericIndex5 = value;
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void setCallerClassName(final String callerClassName) {
        this.callerClassName = callerClassName;
    }

    public void setCallerMethodName(final String callerMethodName) {
        this.callerMethodName = callerMethodName;
    }

    @Override
    public String toString() {
        return "SQueriableLogImpl [tenantId=" + tenantId + ", id=" + id + ", timeStamp=" + timeStamp + ", year=" + year + ", month=" + month + ", dayOfYear="
                + dayOfYear + ", weekOfYear=" + weekOfYear + ", userId=" + userId + ", threadNumber=" + threadNumber + ", clusterNode=" + clusterNode
                + ", productVersion=" + productVersion + ", severity=" + severity + ", actionType=" + actionType + ", actionScope=" + actionScope
                + ", actionStatus=" + actionStatus + ", rawMessage=" + rawMessage + ", callerClassName=" + callerClassName + ", callerMethodName="
                + callerMethodName + ", numericIndex1=" + numericIndex1 + ", numericIndex2=" + numericIndex2 + ", numericIndex3=" + numericIndex3
                + ", numericIndex4=" + numericIndex4 + ", numericIndex5=" + numericIndex5 + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (actionScope == null ? 0 : actionScope.hashCode());
        result = prime * result + actionStatus;
        result = prime * result + (actionType == null ? 0 : actionType.hashCode());
        result = prime * result + (callerClassName == null ? 0 : callerClassName.hashCode());
        result = prime * result + (callerMethodName == null ? 0 : callerMethodName.hashCode());
        result = prime * result + (clusterNode == null ? 0 : clusterNode.hashCode());
        result = prime * result + dayOfYear;
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + month;
        result = prime * result + (int) (numericIndex1 ^ numericIndex1 >>> 32);
        result = prime * result + (int) (numericIndex2 ^ numericIndex2 >>> 32);
        result = prime * result + (int) (numericIndex3 ^ numericIndex3 >>> 32);
        result = prime * result + (int) (numericIndex4 ^ numericIndex4 >>> 32);
        result = prime * result + (int) (numericIndex5 ^ numericIndex5 >>> 32);
        result = prime * result + (productVersion == null ? 0 : productVersion.hashCode());
        result = prime * result + (rawMessage == null ? 0 : rawMessage.hashCode());
        result = prime * result + (severity == null ? 0 : severity.hashCode());
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        result = prime * result + (int) (threadNumber ^ threadNumber >>> 32);
        result = prime * result + (int) (timeStamp ^ timeStamp >>> 32);
        result = prime * result + (userId == null ? 0 : userId.hashCode());
        result = prime * result + weekOfYear;
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SQueriableLogImpl other = (SQueriableLogImpl) obj;
        if (actionScope == null) {
            if (other.actionScope != null) {
                return false;
            }
        } else if (!actionScope.equals(other.actionScope)) {
            return false;
        }
        if (actionStatus != other.actionStatus) {
            return false;
        }
        if (actionType == null) {
            if (other.actionType != null) {
                return false;
            }
        } else if (!actionType.equals(other.actionType)) {
            return false;
        }
        if (callerClassName == null) {
            if (other.callerClassName != null) {
                return false;
            }
        } else if (!callerClassName.equals(other.callerClassName)) {
            return false;
        }
        if (callerMethodName == null) {
            if (other.callerMethodName != null) {
                return false;
            }
        } else if (!callerMethodName.equals(other.callerMethodName)) {
            return false;
        }
        if (clusterNode == null) {
            if (other.clusterNode != null) {
                return false;
            }
        } else if (!clusterNode.equals(other.clusterNode)) {
            return false;
        }
        if (dayOfYear != other.dayOfYear) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (month != other.month) {
            return false;
        }
        if (numericIndex1 != other.numericIndex1) {
            return false;
        }
        if (numericIndex2 != other.numericIndex2) {
            return false;
        }
        if (numericIndex3 != other.numericIndex3) {
            return false;
        }
        if (numericIndex4 != other.numericIndex4) {
            return false;
        }
        if (numericIndex5 != other.numericIndex5) {
            return false;
        }
        if (productVersion == null) {
            if (other.productVersion != null) {
                return false;
            }
        } else if (!productVersion.equals(other.productVersion)) {
            return false;
        }
        if (rawMessage == null) {
            if (other.rawMessage != null) {
                return false;
            }
        } else if (!rawMessage.equals(other.rawMessage)) {
            return false;
        }
        if (severity != other.severity) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        if (threadNumber != other.threadNumber) {
            return false;
        }
        if (timeStamp != other.timeStamp) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        if (weekOfYear != other.weekOfYear) {
            return false;
        }
        if (year != other.year) {
            return false;
        }
        return true;
    }

}
