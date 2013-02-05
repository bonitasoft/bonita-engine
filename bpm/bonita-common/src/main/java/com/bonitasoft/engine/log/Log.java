/*
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.log;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Lu Kai
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public interface Log extends Serializable {

    long getLogId();

    String getMessage();

    SeverityLevel getSeverityLevel();

    String getCreatedBy();

    Date getCreationDate();

    String getActionType();

    String getActionScope();

    SeverityLevel getSeverity();

    String getCallerClassName();

    String getCallerMethodName();

}
