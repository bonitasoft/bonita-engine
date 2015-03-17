import java.util.List;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;

public interface AddressDAO
    extends BusinessObjectDAO
{


    public Address findByCity(String city);

    public List<Address> findByStreet(String street, int startIndex, int maxResults);

    public List<Address> find(int startIndex, int maxResults);

    public Address newInstance();

}