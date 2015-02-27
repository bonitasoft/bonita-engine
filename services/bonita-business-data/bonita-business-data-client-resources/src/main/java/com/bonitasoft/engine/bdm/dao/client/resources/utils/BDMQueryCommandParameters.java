/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.dao.client.resources.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.bonitasoft.engine.bdm.model.field.Field;

public class BDMQueryCommandParameters {

	public static Map<String, Serializable> createCommandParameters(final EntityGetter getter, final long persistenceId) {
        final Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
        commandParameters.put("queryName", getter.getAssociatedNamedQuery());
        commandParameters.put("returnType", getter.getReturnTypeClassName());
        commandParameters.put("returnsList", getter.returnsList());
        commandParameters.put("startIndex", 0);
        commandParameters.put("maxResults", Integer.MAX_VALUE);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put(Field.PERSISTENCE_ID, persistenceId);
        commandParameters.put("queryParameters", (Serializable) queryParameters);
        return commandParameters;
    }
}
