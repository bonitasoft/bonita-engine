package com.bonitasoft.engine.bdm.dao.utils;

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
