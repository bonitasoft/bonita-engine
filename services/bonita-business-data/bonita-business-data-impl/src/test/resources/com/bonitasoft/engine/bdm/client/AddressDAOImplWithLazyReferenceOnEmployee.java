import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bonitasoft.engine.bdm.BusinessObjectDeserializer;
import org.bonitasoft.engine.session.APISession;

public class AddressDAOImpl
    implements AddressDAO
{

    private APISession session;
    private BusinessObjectDeserializer deserializer;

    public AddressDAOImpl(APISession session) {
        this.session = session;
        this.deserializer = new BusinessObjectDeserializer();
    }

    public Address findByCity(String city) {
        try {
            org.bonitasoft.engine.api.CommandAPI commandApi = com.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Address.findByCity");
            commandParameters.put("returnType", "Address");
            commandParameters.put("returnsList", false);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("city", city);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return deserializer.deserialize(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), Address.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Address> findByStreet(String street, int startIndex, int maxResults) {
        try {
            org.bonitasoft.engine.api.CommandAPI commandApi = com.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Address.findByStreet");
            commandParameters.put("returnType", "Address");
            commandParameters.put("returnsList", true);
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            queryParameters.put("street", street);
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), Address.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Address> find(int startIndex, int maxResults) {
        try {
            org.bonitasoft.engine.api.CommandAPI commandApi = com.bonitasoft.engine.api.TenantAPIAccessor.getCommandAPI(session);
            Map<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryName", "Address.find");
            commandParameters.put("returnType", "Address");
            commandParameters.put("returnsList", true);
            commandParameters.put("startIndex", startIndex);
            commandParameters.put("maxResults", maxResults);
            Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
            commandParameters.put("queryParameters", ((Serializable) queryParameters));
            return deserializer.deserializeList(((byte[]) commandApi.execute("executeBDMQuery", commandParameters)), Address.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Address newInstance() {
        Address instance = new Address();
        return instance;
    }

}