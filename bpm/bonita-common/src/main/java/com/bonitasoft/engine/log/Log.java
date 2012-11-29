/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.log;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Lu Kai
 * @author Bole Zhang
 */
public interface Log extends Serializable {

    public long getLogId();

    public String getMessage();

    public SeverityLevel getSeverityLevel();

    public String getCreatedBy();

    public Date getCreationDate();

    public String getActionType();

    public String getActionScope();

    public SeverityLevel getSeverity();

    public String getCallerClassName();

    public String getCallerMethodName();

}
