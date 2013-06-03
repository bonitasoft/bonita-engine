package org.bonitasoft.engine;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.exceptions.ExceptionsManager;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 */
public class ExceptionManagerIntegrationTests extends CommonServiceTest {

    private final static IdentityService identityService;

    private static ExceptionsManager exceptionsManager;

    static {
        identityService = getServicesBuilder().buildIdentityService();
        exceptionsManager = getServicesBuilder().getExceptionsManager();
    }

    @Test
    public void testGetCausesOfUserNotFound() throws Exception {
        try {
            getTransactionService().begin();
            identityService.getUserByUserName("bonita");
        } catch (final SIdentityException e) {
            final List<String> causes = exceptionsManager.getPossibleCauses(e);
            assertEquals(1, causes.size());
            assertEquals("The user with id=? and username=bonita can't be found because it does not exists anymore", causes.get(0));
        } finally {
            getTransactionService().complete();
        }
    }

}
