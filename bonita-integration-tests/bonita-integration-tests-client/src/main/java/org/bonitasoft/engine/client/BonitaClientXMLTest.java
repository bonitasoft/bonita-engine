package org.bonitasoft.engine.client;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.home.BonitaHome;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class BonitaClientXMLTest {

    private static final String BONITA_HOME_CLIENT_INVALID_API_TYPE = "target/bonita_home_client_invalidAPIType";

    private static final String BONITA_HOME_CLIENT_EJB3 = "target/bonita_home_client_EJB3";

    private static final String bonitaHomeStandard = System.getProperty(BonitaHome.BONITA_HOME);

    @BeforeClass
    public static void beforeClass() throws BonitaHomeNotSetException {
        if (bonitaHomeStandard == null) {
            throw new BonitaHomeNotSetException("You must set the system property bonita.home");
        }
    }

    @After
    public void tearDown() {
        TenantAPIAccessor.refresh();
        this.switchBonitaHomeStandard();
    }

    private void switchBonitaHomeStandard() {
        System.setProperty(BonitaHome.BONITA_HOME, bonitaHomeStandard);
    }

    @Test
    public void testGetAPIType() throws Exception {

        ApiAccessType apiType = APITypeManager.getAPIType();
        assertEquals(ApiAccessType.LOCAL, apiType);

        TenantAPIAccessor.refresh();
        System.setProperty(BonitaHome.BONITA_HOME, BONITA_HOME_CLIENT_EJB3);
        apiType = APITypeManager.getAPIType();
        assertEquals(ApiAccessType.EJB3, apiType);
    }

    @Test
    public void testGetAPITypeParameters() throws Exception {
        Map<String, String> parameters = APITypeManager.getAPITypeParameters();
        final Map<String, String> expectedParameters = new HashMap<String, String>();
        assertEquals(expectedParameters, parameters);

        TenantAPIAccessor.refresh();
        System.setProperty(BonitaHome.BONITA_HOME, BONITA_HOME_CLIENT_EJB3);
        parameters = APITypeManager.getAPITypeParameters();
        expectedParameters.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        expectedParameters.put("java.naming.provider.url", "jnp://localhost:1099");
        assertEquals(expectedParameters, parameters);
    }

    @Test(expected = UnknownAPITypeException.class)
    public void testCannotUseAnInvalidAPITypePlatForm() throws Exception {
        System.setProperty(BonitaHome.BONITA_HOME, BONITA_HOME_CLIENT_INVALID_API_TYPE);
        PlatformAPIAccessor.getPlatformLoginAPI();
    }

    @Test(expected = UnknownAPITypeException.class)
    public void testCannotUseAnInvalidAPITypeTenants() throws Exception {
        System.setProperty(BonitaHome.BONITA_HOME, BONITA_HOME_CLIENT_INVALID_API_TYPE);
        TenantAPIAccessor.getLoginAPI();
    }

}
