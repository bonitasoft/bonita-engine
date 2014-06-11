
package org.bonita.hr;

import java.util.List;
import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;

public interface AddressDAO
    extends BusinessObjectDAO
{


    public List<Address> findByStreet(String street, int startIndex, int maxResults);

    public List<Address> findByCity(String city, int startIndex, int maxResults);

    public List<Address> find(int startIndex, int maxResults);

    public List<Address> findAddressesByEmployeePersistenceId(Long persistenceId, int startIndex, int maxResults);

}
