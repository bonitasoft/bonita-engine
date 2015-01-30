/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
import java.util.List;
import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;

public interface AddressDAO
    extends BusinessObjectDAO
{


    public Address findByCity(String city);

    public List<Address> findByStreet(String street, int startIndex, int maxResults);

    public List<Address> find(int startIndex, int maxResults);

    public Address newInstance();

}