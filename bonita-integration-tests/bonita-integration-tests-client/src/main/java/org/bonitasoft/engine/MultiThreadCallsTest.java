package org.bonitasoft.engine;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.test.APITestUtil;
import org.junit.Assert;
import org.junit.Test;

public class MultiThreadCallsTest extends CommonAPITest {

    class CallAPIMethodsThread extends Thread {

        private Exception exception;

        private final APITestUtil apiTestUtil = new APITestUtil();

        public CallAPIMethodsThread() {
        }

        @Override
        public void run() {
            super.run();
            try {
                apiTestUtil.loginOnDefaultTenantWithDefaultTechnicalUser();
                apiTestUtil.getIdentityAPI().getNumberOfUsers();
                apiTestUtil.getIdentityAPI().getNumberOfGroups();
                apiTestUtil.logoutOnTenant();
            } catch (final Exception e) {
                exception = e;
                e.printStackTrace();
            }
        }

        public Exception getException() {
            return exception;
        }

        public String getMessage() {
            if (exception != null) {
                return exception.getMessage();
            }
            return "";
        }
    }

    @Test
    public void supportMultiThreadingClients() throws Exception {
        final int nbOfThreads = 5;
        final List<CallAPIMethodsThread> threads = new ArrayList<MultiThreadCallsTest.CallAPIMethodsThread>(nbOfThreads);
        for (int i = 0; i < nbOfThreads; i++) {
            threads.add(new CallAPIMethodsThread());
        }
        for (final CallAPIMethodsThread thread : threads) {
            thread.start();
        }
        for (final CallAPIMethodsThread thread : threads) {
            thread.join();
        }
        for (final CallAPIMethodsThread thread : threads) {
            Assert.assertNull(thread.getMessage(), thread.getException());
        }
    }
}
