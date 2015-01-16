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
