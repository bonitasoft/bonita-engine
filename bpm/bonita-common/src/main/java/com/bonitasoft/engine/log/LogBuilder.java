/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
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
        entity = new LogImpl();
        entity.setMessage(message);
        entity.setCreatedBy(createdBy);
        entity.setCreationDate(creationDate);
        return this;
    }

    public LogBuilder setLogId(final long logId) {
        entity.setLogId(logId);
        return this;
    }

    public LogBuilder setSeverityLevel(final SeverityLevel severityLevel) {
        entity.setSeverityLevel(severityLevel);
        return this;
    }

    public LogBuilder setSeverity(final SeverityLevel severity) {
        entity.setSeverity(severity);
        return this;
    }

    public LogBuilder setActionScope(final String actionScope) {
        entity.setActionScope(actionScope);
        return this;
    }

    public LogBuilder setActionType(final String actionType) {
        entity.setActionType(actionType);
        return this;
    }

    public LogBuilder setCallerClassName(final String callerClassName) {
        entity.setCallerClassName(callerClassName);
        return this;
    }

    public LogBuilder setCallerMethodName(final String callerMethodName) {
        entity.setCallerMethodName(callerMethodName);
        return this;
    }

    public Log done() {
        return entity;
    }

}
