package com.bonitasoft.engine.bdm.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LazyLoader {

    private CommandAPI commandApi;

    public LazyLoader(final APISession apiSession) {
        if (apiSession == null) {
            throw new IllegalArgumentException("apiSession cannot be null");
        }
        try {
            commandApi = TenantAPIAccessor.getCommandAPI(apiSession);
        } catch (final BonitaHomeNotSetException e) {
            throw new IllegalArgumentException(e);
        } catch (final ServerAPIException e) {
            throw new IllegalArgumentException(e);
        } catch (final UnknownAPITypeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Object load(final Method method, final long persistenceId) {
        try {
            final Map<String, Serializable> commandParameters = createCommandParameters(method, persistenceId);
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JavaType javaType = null;
            if (returnsAList(method)) {
                final ParameterizedType listType = (ParameterizedType) method.getGenericReturnType();
                javaType = mapper.getTypeFactory().constructCollectionType(List.class,
                        mapper.getTypeFactory().constructType(listType.getActualTypeArguments()[0]));
            } else {
                javaType = mapper.getTypeFactory().constructType(method.getClass());
            }

            return mapper.readValue((byte[]) commandApi.execute("executeBDMQuery", commandParameters), javaType);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Map<String, Serializable> createCommandParameters(final Method method, final long persistenceId) {
        final Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
        commandParameters.put("queryName", "Address.findByStreet");
        commandParameters.put("returnType", "Address");
        commandParameters.put("returnsList", returnsAList(method));
        commandParameters.put("startIndex", 0);
        commandParameters.put("maxResults", Integer.MAX_VALUE);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put(Field.PERSISTENCE_ID, persistenceId);
        commandParameters.put("queryParameters", (Serializable) queryParameters);
        return commandParameters;
    }

    private boolean returnsAList(final Method method) {
        final Class<?> returnTypeClass = method.getReturnType();
        return Collection.class.isAssignableFrom(returnTypeClass);
    }
}
