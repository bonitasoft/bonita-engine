/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package com.bonitasoft.engine.log;

import java.util.Date;

/**
 * @author Bole Zhang
 */
public class LogBuilder {

    private LogImpl entity;

    public LogBuilder() {
        super();
    }

    public LogBuilder createNewInstance(final String message, final String createdBy, final Date creationDate) {
        this.entity = new LogImpl();
        this.entity.setMessage(message);
        this.entity.setCreatedBy(createdBy);
        this.entity.setCreationDate(creationDate);
        return this;
    }

    public LogBuilder setLogId(final long logId) {
        this.entity.setLogId(logId);
        return this;
    }

    public LogBuilder setSeverityLevel(final SeverityLevel severityLevel) {
        this.entity.setSeverityLevel(severityLevel);
        return this;
    }

    public LogBuilder setSeverity(final SeverityLevel severity) {
        this.entity.setSeverity(severity);
        return this;
    }

    public LogBuilder setActionScope(final String actionScope) {
        this.entity.setActionScope(actionScope);
        return this;
    }

    public LogBuilder setActionType(final String actionType) {
        this.entity.setActionType(actionType);
        return this;
    }

    public LogBuilder setCallerClassName(final String callerClassName) {
        this.entity.setCallerClassName(callerClassName);
        return this;
    }

    public LogBuilder setCallerMethodName(final String callerMethodName) {
        this.entity.setCallerMethodName(callerMethodName);
        return this;
    }

    public Log done() {
        return this.entity;
    }

}
