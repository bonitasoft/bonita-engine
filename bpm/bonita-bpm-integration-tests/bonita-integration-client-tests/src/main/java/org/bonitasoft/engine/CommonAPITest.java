package org.bonitasoft.engine;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommonAPITest extends APITestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonAPITest.class);

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        APITestUtil.initializeAndStartPlatformWithDefaultTenant(true);
    }

    @AfterClass
    public static void afterClass() throws BonitaException, InterruptedException {
        APITestUtil.stopAndCleanPlatformAndTenant(true);
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + d.getClassName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            try {
                LOGGER.error("Failed test: " + d.getClassName() + "." + d.getMethodName());
                clean();
            } catch (final Exception be) {
                LOGGER.error("Unable to clean db", be);
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }

        @Override
        public void succeeded(final Description d) {
            List<String> clean = null;
            try {
                clean = clean();
            } catch (final BonitaException e) {
                throw new BonitaRuntimeException(e);
            }
            LOGGER.info("Succeeded test: " + d.getClassName() + "." + d.getMethodName());
            LOGGER.info("-----------------------------------------------------------------------------------------------");
            if (!clean.isEmpty()) {
                throw new BonitaRuntimeException(clean.toString());
            }
        }
    };

    /**
     * FIXME: clean actors!
     * 
     * @return
     * @throws BonitaException
     */
    private List<String> clean() throws BonitaException {
        login();

        final List<String> messages = new ArrayList<String>();
        messages.addAll(checkExistenceOfCommands());
        messages.addAll(checkExistenceOfUsers());
        messages.addAll(checkExistenceOfGroups());
        messages.addAll(checkExistenceOfRoles());
        messages.addAll(checkExistenceOfProcessDefinitions());
        messages.addAll(checkExistenceOfProcessIntances());
        messages.addAll(checkExistenceOfFlowNodes());
        messages.addAll(checkExistenceOfCategories());

        logout();
        return messages;
    }

    public BarResource getResource(final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }

}
