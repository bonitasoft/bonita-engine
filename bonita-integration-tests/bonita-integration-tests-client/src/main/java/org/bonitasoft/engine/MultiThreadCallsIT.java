/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.test.APITestUtil;
import org.junit.Assert;
import org.junit.Test;

public class MultiThreadCallsIT extends CommonAPIIT {

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
        final List<CallAPIMethodsThread> threads = new ArrayList<MultiThreadCallsIT.CallAPIMethodsThread>(nbOfThreads);
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
