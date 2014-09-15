package org.bonitasoft.engine.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.bonitasoft.engine.ServicesBuilder;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformSessionServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(PlatformSessionServiceTest.class);

    private static TransactionService txService;

    private static ServicesBuilder servicesBuilder;

    private static PlatformSessionService sessionService;

    static {
        servicesBuilder = new ServicesBuilder();
        txService = servicesBuilder.buildTransactionService();
        sessionService = servicesBuilder.buildPlatformSessionService();
    }

    @AfterClass
    public static void tearDownPersistence() throws Exception {
        TestUtil.closeTransactionIfOpen(txService);
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOGGER.info("Failed test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + this.getClass().getName() + "." + d.getMethodName());
        }

    };

    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(txService);
    }

    @Test
    public void testCreateSession() throws Exception {
        final String username = "platformAdmin";
        final Date before = new Date();
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        assertNotNull(session.getCreationDate());
        assertTrue(before.getTime() <= session.getCreationDate().getTime());
        assertTrue(session.getDuration() > 0);
        assertEquals(sessionService.getSessionsDuration(), session.getDuration());
        assertEquals(session.getLastRenewDate().getTime() + session.getDuration(), session.getExpirationDate().getTime());
        assertEquals(username, session.getUserName());
    }

    @Test
    public void testIsValid() throws Exception {
        final String username = "platformAdmin";
        sessionService.setSessionDuration(1000);
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        assertEquals(1000, session.getDuration());
        assertTrue(sessionService.isValid(session.getId()));
        Thread.sleep(session.getDuration() + 1);
        assertFalse(sessionService.isValid(session.getId()));
    }

    @Test
    public void testGetSession() throws Exception {
        final String username = "platformAdmin";
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        final SPlatformSession retrievedSession = sessionService.getSession(session.getId());
        assertEquals(session, retrievedSession);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testDeleteInvalidSession() throws Exception {
        sessionService.deleteSession(System.currentTimeMillis());
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testDeleteSession() throws Exception {
        final String username = "platformAdmin";
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        SPlatformSession retrievedSession = sessionService.getSession(session.getId());
        assertEquals(session, retrievedSession);

        sessionService.deleteSession(session.getId());
        retrievedSession = sessionService.getSession(session.getId());
    }

    @Test
    public void testrenewSession() throws Exception {
        final String username = "matti";
        // txService.begin();
        final SPlatformSession session = sessionService.createSession(username);
        // txService.complete();
        Thread.sleep(100);

        // txService.begin();
        sessionService.renewSession(session.getId());
        // txService.complete();

        // txService.begin();
        final SPlatformSession session2 = sessionService.getSession(session.getId());
        // txService.complete();
        assertTrue(session2.getExpirationDate().after(session.getExpirationDate()));
        assertTrue(session2.getLastRenewDate().after(session.getLastRenewDate()));
        assertEquals(session2.getLastRenewDate().getTime() + session2.getDuration(), session2.getExpirationDate().getTime());
    }

}
