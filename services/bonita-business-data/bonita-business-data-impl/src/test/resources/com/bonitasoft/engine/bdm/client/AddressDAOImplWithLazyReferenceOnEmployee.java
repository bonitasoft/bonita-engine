/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer;
import com.bonitasoft.engine.bdm.dao.client.resources.proxy.LazyLoader;
import com.bonitasoft.engine.bdm.dao.client.resources.proxy.Proxyfier;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.session.APISession;

public class AddressDAOImpl
    implements AddressDAO
{

    private APISession session;
    private BusinessObjectDeserializer deserializer;
    private Proxyfier proxyfier;

    public AddressDAOImpl(APISession session) {
        this.session = session;
        this.deserializer = new BusinessObjectDeserializer();
        LazyLoader lazyLoader = new LazyLoader(session);
        this.proxyfier = new Proxyfier(lazyLoader);
    }

    public Address findByCity(String city) {
        try {
            CommandAPI commandApi = com.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Address.findByCity");
            commandParameters.put("returnsList", false);
            commandParameters.put("returnType", "Address");
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("city", city);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserialize(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), Address.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Address> findByStreet(String street, int startIndex, int maxResults) {
        try {
            CommandAPI commandApi = com.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Address.findByStreet");
            commandParameters.put("returnsList", true);
            commandParameters.put("returnType", "Address");
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("street", street);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), Address.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Address> find(int startIndex, int maxResults) {
        try {
            CommandAPI commandApi = com.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Address.find");
            commandParameters.put("returnsList", true);
            commandParameters.put("returnType", "Address");
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return proxyfier.proxify(deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), Address.class));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Address newInstance() {
        Address instance = new Address();
        return instance;
    }

}