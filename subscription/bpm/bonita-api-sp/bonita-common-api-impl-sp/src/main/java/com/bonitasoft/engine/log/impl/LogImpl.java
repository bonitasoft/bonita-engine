/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.impl;

import java.util.Date;

import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.SeverityLevel;

/**
 * @author Matthieu Chaffotte
 */
public class LogImpl implements Log {

    private static final long serialVersionUID = 3783837487970304334L;

    private final long logId;

    private final String message;

    private SeverityLevel severityLevel;

    private String createdBy;

    private Date creationDate;

    private String actionType;

    private String actionScope;

    private String callerClassName;

    private String callerMethodName;

    public LogImpl(final long logId, final String message) {
        super();
        this.logId = logId;
        this.message = message;
    }

    public void setSeverityLevel(final SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setActionScope(final String actionScope) {
        this.actionScope = actionScope;
    }

    public void setActionType(final String actionType) {
        this.actionType = actionType;
    }

    public void setCallerClassName(final String callerClassName) {
        this.callerClassName = callerClassName;
    }

    public void setCallerMethodName(final String callerMethodName) {
        this.callerMethodName = callerMethodName;
    }

    @Override
    public long getLogId() {
        return logId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
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
    public String getCallerClassName() {
        return callerClassName;
    }

    @Override
    public String getCallerMethodName() {
        return callerMethodName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (actionScope == null ? 0 : actionScope.hashCode());
        result = prime * result + (actionType == null ? 0 : actionType.hashCode());
        result = prime * result + (callerClassName == null ? 0 : callerClassName.hashCode());
        result = prime * result + (callerMethodName == null ? 0 : callerMethodName.hashCode());
        result = prime * result + (createdBy == null ? 0 : createdBy.hashCode());
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (int) (logId ^ logId >>> 32);
        result = prime * result + (message == null ? 0 : message.hashCode());
        result = prime * result + (severityLevel == null ? 0 : severityLevel.hashCode());
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
        final LogImpl other = (LogImpl) obj;
        if (actionScope == null) {
            if (other.actionScope != null) {
                return false;
            }
        } else if (!actionScope.equals(other.actionScope)) {
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
        if (createdBy == null) {
            if (other.createdBy != null) {
                return false;
            }
        } else if (!createdBy.equals(other.createdBy)) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (logId != other.logId) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (severityLevel != other.severityLevel) {
            return false;
        }
        return true;
    }

}
