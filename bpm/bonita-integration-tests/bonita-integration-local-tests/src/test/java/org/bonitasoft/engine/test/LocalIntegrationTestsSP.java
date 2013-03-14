/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.test;

import java.sql.SQLException;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bonitasoft.engine.BPMSPTests;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

@RunWith(Suite.class)
@SuiteClasses({
        BPMSPTests.class,
        BPMLocalSuiteTests.class,
        LocalLogTest.class,
        APIMethodSPTest.class,
        ConnectorExecutionTimeOutTest.class
})
public class LocalIntegrationTestsSP {

    static ConfigurableApplicationContext contextJNDI;
    static Server server;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        System.err.println("=================== LocalIntegrationTestsSP.beforeClass()");

        setupH2();
        setupJNDI();

        PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.createPlatform();
        platformLoginAPI.logout(session);

        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        System.err.println("=================== LocalIntegrationTestsSP.afterClass()");
        PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deletePlaftorm();
        platformLoginAPI.logout(session);

        shutdownJNDI();
        shutdownH2();
    }

    private static void setupH2() throws BonitaException {
        // start the TCP Server
        try {
            server = Server.createTcpServer(new String[] { "-tcpAllowOthers" }).start();
        } catch (SQLException e) {
            throw new BonitaException(e);
        }
    }
    
    private static void shutdownH2() {
        server.stop();
    }
    
    
    private static void setupJNDI() {
        System.setProperty("java.naming.factory.initial", "org.ow2.carol.jndi.spi.URLInitialContextFactory");
        System.setProperty("java.naming.provider.url", "rmi://localhost:" + System.getProperty("jndi.carol.port", "1099"));
        contextJNDI = new ClassPathXmlApplicationContext("jndi-setup.xml");
    }

    private static void shutdownJNDI() {
        contextJNDI.close();
    }
}
