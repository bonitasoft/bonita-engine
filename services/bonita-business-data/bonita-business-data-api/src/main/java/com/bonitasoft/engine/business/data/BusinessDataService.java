/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import java.io.Serializable;

public interface BusinessDataService {

    Object callJavaOperation(Object businessObject, Object valueToSetObjectWith, String methodName, String parameterType)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException;

    boolean isBusinessData(Object valueToSetObjectWith);

    Serializable getJsonEntity(String entityClassName, Long identifier, String businessDataURIPattern) throws SBusinessDataNotFoundException,
            SBusinessDataRepositoryException;

    Serializable getJsonChildEntity(String entityClassName, Long identifier, String childName, String businessDataURIPattern) throws SBusinessDataNotFoundException,
            SBusinessDataRepositoryException;

}
