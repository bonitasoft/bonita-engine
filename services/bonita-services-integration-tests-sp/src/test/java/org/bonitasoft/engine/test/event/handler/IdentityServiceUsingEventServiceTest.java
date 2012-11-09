/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.event.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.UserUpdateEventHandler;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.ServicesBuilder;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
public class IdentityServiceUsingEventServiceTest {

    private static IdentityService identityService;

    private static IdentityModelBuilder builder;

    private static TransactionService txService;

    private static ServicesBuilder servicesBuilder;

    private static PlatformService platformService;

    private static SPlatformBuilder platformBuilder;

    private static STenantBuilder tenantBuilder;

    private static SessionAccessor sessionAccessor;

    private static EventService eventService;

    private static SessionService sessionService;

    static {
        servicesBuilder = new ServicesBuilder();
        txService = servicesBuilder.buildTransactionService();
        identityService = servicesBuilder.buildIdentityService();
        builder = servicesBuilder.buildIdentityModelBuilder();
        platformService = servicesBuilder.buildPlatformService();
        platformBuilder = servicesBuilder.buildPlatformBuilder();
        tenantBuilder = servicesBuilder.buildTenantBuilder();
        sessionAccessor = servicesBuilder.buildSessionAccessor();
        eventService = servicesBuilder.buildEventService();
        sessionService = servicesBuilder.buildSessionService();
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        private final Logger LOGGER = LoggerFactory.getLogger(RecorderAndEventServiceTest.class);

        @Override
        public void starting(final Description d) {
            LOGGER.error("Starting test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            LOGGER.error("Failed test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.error("Succeeded test: " + this.getClass().getName() + "." + d.getMethodName());
        }
    };

    @BeforeClass
    public static void setUpPersistence() throws Exception {
        TestUtil.createPlatformAndDefaultTenant(txService, platformService, sessionAccessor, platformBuilder, tenantBuilder, sessionService);
    }

    @AfterClass
    public static void tearDownPersistence() throws Exception {
        TestUtil.closeTransactionIfOpen(txService);
        TestUtil.deleteDefaultTenantAndPlatForm(txService, platformService, sessionAccessor, sessionService);
    }

    @Test
    public void testUserPasswordUpdate() throws Exception {
        BusinessTransaction tx = txService.createTransaction();
        tx.begin();
        final String userName = "Zhang";
        final SUser user = builder.getUserBuilder().createNewInstance().setUserName(userName).setPassword("oldpassword").setFirstName("bole")
                .setLastName("zhang").done();
        identityService.createUser(user);
        tx.complete();

        tx = txService.createTransaction();
        tx.begin();
        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        final EntityUpdateDescriptor changeDescriptor = builder.getUserUpdateBuilder().updatePassword("newpassword").done();
        identityService.updateUser(user, changeDescriptor);

        assertEquals(user.getPassword(), userUpdateEventHandler.getPassword(userName));

        identityService.deleteUser(user);
        tx.complete();
    }

    @Test
    public void testUserPasswordNotUpdate() throws Exception {
        BusinessTransaction tx = txService.createTransaction();
        tx.begin();

        final String userName = "Zhang";
        final SUser user = builder.getUserBuilder().createNewInstance().setUserName(userName).setPassword("oldpassword").setFirstName("bole")
                .setLastName("zhang").done();
        identityService.createUser(user);
        tx.complete();

        tx = txService.createTransaction();
        tx.begin();
        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        final EntityUpdateDescriptor changeDescriptor = builder.getUserUpdateBuilder().updatePassword("oldpassword").done();
        identityService.updateUser(user, changeDescriptor);

        assertNull(userUpdateEventHandler.getPassword(userName));
        identityService.deleteUser(user);
        tx.complete();
    }

    @Test
    public void testUpdateUserWithoutPasswordChange() throws Exception {
        BusinessTransaction tx = txService.createTransaction();
        tx.begin();

        final SUser user = builder.getUserBuilder().createNewInstance().setUserName("testUpdateUser").setPassword("kikoo").setFirstName("Update")
                .setLastName("User").done();
        identityService.createUser(user);
        tx.complete();

        tx = txService.createTransaction();
        tx.begin();
        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        final EntityUpdateDescriptor changeDescriptor = builder.getUserUpdateBuilder().updateUserName("testUpdateUser2").updateFirstName("updated")
                .updateLastName("user2").done();
        identityService.updateUser(user, changeDescriptor);

        assertNull(userUpdateEventHandler.getPassword("testUpdateUser2"));
        identityService.deleteUser(user);
        tx.complete();
    }

    private UserUpdateEventHandler resetUserPasswordUpdateEventHandler(final EventService eventService) {
        final UserUpdateEventHandler userPasswordHandler = (UserUpdateEventHandler) eventService.getHandlers("USER_UPDATED").toArray()[0];
        userPasswordHandler.cleanUserMap();
        return userPasswordHandler;
    }

}
