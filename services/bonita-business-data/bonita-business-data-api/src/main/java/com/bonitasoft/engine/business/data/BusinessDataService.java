package com.bonitasoft.engine.business.data;

import java.io.Serializable;
import java.util.Map;

public interface BusinessDataService {

    Object callJavaOperation(Object businessObject, Object valueToSetObjectWith, String methodName, String parameterType)
            throws SBusinessDataNotFoundException, SBusinessDataRepositoryException;

    boolean isBusinessData(Object valueToSetObjectWith);

    Serializable getJsonEntity(String entityClassName, Long identifier, String businessDataURIPattern) throws SBusinessDataNotFoundException,
            SBusinessDataRepositoryException;

    Serializable getJsonChildEntity(String entityClassName, Long identifier, String childName, String businessDataURIPattern)
            throws SBusinessDataNotFoundException,
            SBusinessDataRepositoryException;

    Serializable getJsonQueryEntities(String entityClassName, String queryName, Map<String, Serializable> queryParameters, Integer startIndex, Integer maxResults, String businessDataURIPattern)
            throws SBusinessDataRepositoryException;

}
