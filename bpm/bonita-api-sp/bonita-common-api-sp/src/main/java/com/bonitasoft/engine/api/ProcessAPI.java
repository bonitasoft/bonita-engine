/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.io.Serializable;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * @author Matthieu Chaffotte
 */
public interface ProcessAPI extends org.bonitasoft.engine.api.ProcessAPI, ProcessManagementAPI, ProcessRuntimeAPI {

    /**
     * Gets the value of named business data item of the process instance.
     * 
     * @param dataName
     *            the name of the data item.
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return an instance of the data
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *             if the specified data value cannot be found.
     * @since 6.0
     */
    Serializable getBusinessDataInstance(String dataName, long processInstanceId) throws DataNotFoundException;

}
