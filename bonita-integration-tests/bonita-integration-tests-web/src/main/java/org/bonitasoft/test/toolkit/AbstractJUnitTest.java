/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.test.toolkit;

import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.bonitasoft.test.toolkit.organization.AdminUser;
import org.bonitasoft.test.toolkit.organization.TestToolkitCtx;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

/**
 * Template of JUnit Test. Do initialization as well as necessary clean up.
 *
 * @author Vincent Elcrin, Anthony Birembaut
 */
public abstract class AbstractJUnitTest {

    @Rule
    public BonitaEngineRule bonitaEngineRule = createBonitaEngineRule();

    protected BonitaEngineRule createBonitaEngineRule() {
        return BonitaEngineRule.create().withCleanAfterTest();
    }

    @Before
    public final void aaSetUp() throws Exception {
        getContext().check();
        getContext().setInitiator(getInitiator());
        testSetUp();
    }

    @After
    public final void zzTearDown() throws Exception {
        testTearDown();
        final AdminUser adminUser = TestToolkitCtx.getInstance().getAdminUser();
        for (TestUser testUser : TestUserFactory.getInstance().getUserList().values()) {
            adminUser.delete(testUser);
        }
        getContext().clearSession();
    }

    /**
     * Initiator is a convenient notion to set a test user as source of all transactions done with the framework via its
     * API Session.
     * It still can be overridden for all methods of the framework and also can be set for the session with
     * {@link TestToolkitCtx#setInitiator(TestUser)}
     *
     * @return
     * @throws Exception
     */
    protected abstract TestUser getInitiator();

    /**
     * Define context to use during the test. Two implementations exist. TestToolkitCtx and TestToolkitCtx (SP version)
     *
     * @return
     */
    protected abstract TestToolkitCtx getContext();

    /**
     * JUnit's {@link Before} implementation.
     *
     * @throws Exception
     */
    protected abstract void testSetUp() throws Exception;

    /**
     * JUnit's {@link After} implementation.
     *
     * @throws Exception
     */
    protected abstract void testTearDown() throws Exception;
}
