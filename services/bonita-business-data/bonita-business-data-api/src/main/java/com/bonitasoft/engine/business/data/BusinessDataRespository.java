/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public interface BusinessDataRespository {

    void start() throws SBusinessDataRepositoryDeploymentException;

    void stop();

    <T> T find(Class<T> entityClass, Serializable primaryKey) throws BusinessDataNotFoundException;

    <T> T find(Class<T> resultClass, String qlString, Map<String, Object> parameters) throws BusinessDataNotFoundException, NonUniqueResultException;

    void persist(Object entity);

    void deploy(byte[] bdrArchive, long tenantId) throws SBusinessDataRepositoryDeploymentException;

}
