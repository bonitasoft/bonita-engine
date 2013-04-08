/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.event.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.UserUpdateEventHandler;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class IdentityServiceUsingEventServiceTest extends CommonServiceTest {

    private static IdentityService identityService;

    private static IdentityModelBuilder builder;

    private static EventService eventService;

    static {
        identityService = getServicesBuilder().buildIdentityService();
        builder = getServicesBuilder().buildIdentityModelBuilder();
        eventService = getServicesBuilder().buildEventService();
    }

    @Test
    public void testUserPasswordUpdate() throws Exception {
        BusinessTransaction tx = getTransactionService().createTransaction();
        tx.begin();
        final String userName = "Zhang";
        final SUserBuilder userBuilder = builder.getUserBuilder();
        userBuilder.createNewInstance().setUserName(userName).setPassword("oldpassword").setFirstName("bole").setLastName("zhang");
        SUser user = identityService.createUser(userBuilder.done());
        tx.complete();

        tx = getTransactionService().createTransaction();
        tx.begin();
        user = identityService.getUser(user.getId());

        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        final EntityUpdateDescriptor changeDescriptor = builder.getUserUpdateBuilder().updatePassword("newpassword").done();
        identityService.updateUser(user, changeDescriptor);

        user = identityService.getUser(user.getId());
        assertEquals(user.getPassword(), userUpdateEventHandler.getPassword(userName));

        identityService.deleteUser(user);
        tx.complete();
    }

    @Test
    public void testUserPasswordNotUpdate() throws Exception {
        BusinessTransaction tx = getTransactionService().createTransaction();
        tx.begin();

        final String userName = "Zhang";
        final SUserBuilder userBuilder = builder.getUserBuilder();
        userBuilder.createNewInstance().setUserName(userName).setPassword("oldpassword").setFirstName("bole").setLastName("zhang");
        SUser user = identityService.createUser(userBuilder.done());
        tx.complete();

        tx = getTransactionService().createTransaction();
        tx.begin();
        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        user = identityService.getUser(user.getId());
        final EntityUpdateDescriptor changeDescriptor = builder.getUserUpdateBuilder().updatePassword("oldpassword").done();
        identityService.updateUser(user, changeDescriptor);

        assertNull(userUpdateEventHandler.getPassword(userName));
        identityService.deleteUser(user);
        tx.complete();
    }

    @Test
    public void testUpdateUserWithoutPasswordChange() throws Exception {
        BusinessTransaction tx = getTransactionService().createTransaction();
        tx.begin();
        final SUserBuilder userBuilder = builder.getUserBuilder();
        userBuilder.createNewInstance().setUserName("testUpdateUser").setPassword("kikoo").setFirstName("Update").setLastName("User");
        SUser user = identityService.createUser(userBuilder.done());
        tx.complete();

        tx = getTransactionService().createTransaction();
        tx.begin();
        user = identityService.getUser(user.getId());
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
