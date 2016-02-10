/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log;

import java.util.Date;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * @author Lu Kai
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public interface Log extends BonitaObject {

    long getLogId();

    String getMessage();

    SeverityLevel getSeverityLevel();

    String getCreatedBy();

    Date getCreationDate();

    String getActionType();

    String getActionScope();

    String getCallerClassName();

    String getCallerMethodName();

}
