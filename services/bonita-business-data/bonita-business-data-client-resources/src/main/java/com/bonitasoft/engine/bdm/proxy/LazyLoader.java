package com.bonitasoft.engine.bdm.proxy;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bdm.BusinessObjectDeserializer;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class LazyLoader {

    private final APISession apiSession;
    private final BusinessObjectDeserializer deserializer;

    public LazyLoader(final APISession apiSession) {
        if (apiSession == null) {
            throw new IllegalArgumentException("apiSession cannot be null");
        }
        this.apiSession = apiSession;
        deserializer = new BusinessObjectDeserializer();
    }

    public Object load(final Method method, final long persistenceId) {
        try {
            final CommandAPI commandApi = TenantAPIAccessor.getCommandAPI(apiSession);
            final Map<String, Serializable> commandParameters = createCommandParameters(method, persistenceId);
            final byte[] serializedResult = (byte[]) commandApi.execute("executeBDMQuery", commandParameters);
            return deserialize(method, serializedResult);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Object deserialize(final Method method, final byte[] serializedResult) throws IOException, JsonParseException, JsonMappingException {
        Class<?> javaType = null;
        if (returnsAList(method)) {
            final ParameterizedType listType = (ParameterizedType) method.getGenericReturnType();
            javaType = (Class<?>) listType.getActualTypeArguments()[0];
            return deserializer.deserializeList(serializedResult,  javaType);
        } else {
            javaType = method.getReturnType();
            return deserializer.deserialize(serializedResult,  javaType);
        }
    }

    protected Map<String, Serializable> createCommandParameters(final Method method, final long persistenceId) {
        final Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
        commandParameters.put("queryName", getQueryNameFor(method));
        commandParameters.put("returnType", getReturnTypeClassname(method));
        commandParameters.put("returnsList", returnsAList(method));
        commandParameters.put("startIndex", 0);
        commandParameters.put("maxResults", Integer.MAX_VALUE);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put(Field.PERSISTENCE_ID, persistenceId);
        commandParameters.put("queryParameters", (Serializable) queryParameters);
        return commandParameters;
    }

    protected Serializable getQueryNameFor(final Method method) {
        final String targetEntityName = getTargetEntityNameFrom(method);
        final String sourceEntityName = getSourceEntityNameFrom(method);
        final String name = toFieldName(method.getName());
        return targetEntityName + ".find" + name + "By" + sourceEntityName + WordUtils.capitalize(Field.PERSISTENCE_ID);
    }

    private String toFieldName(final String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return methodName.substring(3);
        }
        throw new IllegalArgumentException(methodName + " is not a valid getter name.");
    }

    private String getTargetEntityNameFrom(final Method method) {
        if(returnsAList(method)){
            final ParameterizedType listType = (ParameterizedType) method.getGenericReturnType();
            final Class<?> type = (Class<?>) listType.getActualTypeArguments()[0];
            return type.getSimpleName();
        }else{
            return method.getReturnType().getSimpleName();
        }
    }

    private String getSourceEntityNameFrom(final Method method) {
        return method.getDeclaringClass().getSimpleName();
    }

    private Serializable getReturnTypeClassname(final Method method) {
        if (returnsAList(method)) {
            return List.class.getName();
        } else {
            return method.getReturnType().getName();
        }
    }

    private boolean returnsAList(final Method method) {
        final Class<?> returnTypeClass = method.getReturnType();
        return Collection.class.isAssignableFrom(returnTypeClass);
    }
}
