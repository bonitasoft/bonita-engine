package com.bonitasoft.engine.bdm.dao.client.resources.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer;
import com.bonitasoft.engine.bdm.dao.client.resources.utils.BDMQueryCommandParameters;
import com.bonitasoft.engine.bdm.dao.client.resources.utils.EntityGetter;

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
            EntityGetter getter = new EntityGetter(method);
            final Map<String, Serializable> commandParameters = BDMQueryCommandParameters.createCommandParameters(getter, persistenceId);
            final byte[] serializedResult = (byte[]) getCommandAPI().execute("executeBDMQuery", commandParameters);
            if (getter.returnsList()) {
                return deserializer.deserializeList(serializedResult, getter.getTargetEntityClass());
            } else {
                return deserializer.deserialize(serializedResult,  getter.getTargetEntityClass());
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * protected for testing 
     */
    protected CommandAPI getCommandAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getCommandAPI(apiSession);
    }
}
