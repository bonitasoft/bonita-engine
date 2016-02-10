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
package org.bonitasoft.engine.platform;

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.PrintTestsStatusRule;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformLoginIT extends CommonAPIIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformLoginIT.class);

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        logAsPlatformAdmin();
    }

    private static void logAsPlatformAdmin() throws PlatformLoginException {
        session = platformLoginAPI.login("platformAdmin", "platform");
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformLoginAPI.logout(session);
    }

    @Rule
    public TestRule testWatcher = new PrintTestsStatusRule(LOGGER) {
        @Override
        public List<String> clean() throws Exception {
            return Collections.emptyList();
        }
    };

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Login" }, story = "Try to log with wrong loggin.", jira = "")
    @Test(expected = InvalidPlatformCredentialsException.class)
    public void wrongLogin() throws BonitaException {
        try {
            platformLoginAPI.logout(session);
            platformLoginAPI.login("titi", "toto");
            fail();
        } finally {
            logAsPlatformAdmin();
        }
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Login" }, story = "Try to log with wrong loggin.", jira = "")
    @Test(expected = SessionNotFoundException.class)
    public void logoutWithWrongSession() throws BonitaException {
        try {
            platformLoginAPI.logout(new PlatformSessionImpl(123L, null, -1L, null, -1L));
        } finally {
            platformLoginAPI.logout(session);
            logAsPlatformAdmin();
        }
    }

}
