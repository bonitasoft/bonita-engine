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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.UserUpdateEventHandler;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

import com.bonitasoft.services.CommonServiceSPTest;

/**
 * @author Elias Ricken de Medeiros
 */
public class IdentityServiceUsingEventServiceTest extends CommonServiceSPTest {

    private static IdentityService identityService;

    private static EventService eventService;

    static {
        identityService = getServicesBuilder().buildIdentityService();
        eventService = getServicesBuilder().buildEventService();
    }

    @Test
    public void testUserPasswordUpdate() throws Exception {
        getTransactionService().begin();
        final String userName = "Zhang";
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(userName).setPassword("oldpassword").setFirstName("bole").setLastName("zhang");
        SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();

        getTransactionService().begin();
        user = identityService.getUser(user.getId());

        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updatePassword("newpassword").done();
        identityService.updateUser(user, changeDescriptor);

        user = identityService.getUser(user.getId());
        assertEquals(user.getPassword(), userUpdateEventHandler.getPassword(userName));

        identityService.deleteUser(user);
        getTransactionService().complete();
    }

    @Test
    public void testUserPasswordNotUpdate() throws Exception {
        getTransactionService().begin();

        final String userName = "Zhang";
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(userName).setPassword("oldpassword").setFirstName("bole").setLastName("zhang");
        SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();

        getTransactionService().begin();
        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        user = identityService.getUser(user.getId());
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updatePassword("oldpassword").done();
        identityService.updateUser(user, changeDescriptor);

        assertNull(userUpdateEventHandler.getPassword(userName));
        identityService.deleteUser(user);
        getTransactionService().complete();
    }

    @Test
    public void testUpdateUserWithoutPasswordChange() throws Exception {
        getTransactionService().begin();
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testUpdateUser").setPassword("kikoo").setFirstName("Update").setLastName("User");
        SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();

        getTransactionService().begin();
        user = identityService.getUser(user.getId());
        final UserUpdateEventHandler userUpdateEventHandler = resetUserPasswordUpdateEventHandler(eventService);

        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updateUserName("testUpdateUser2").updateFirstName("updated")
                .updateLastName("user2").done();
        identityService.updateUser(user, changeDescriptor);

        assertNull(userUpdateEventHandler.getPassword("testUpdateUser2"));
        identityService.deleteUser(user);
        getTransactionService().complete();
    }

    private UserUpdateEventHandler resetUserPasswordUpdateEventHandler(final EventService eventService) {
        final UserUpdateEventHandler userPasswordHandler = (UserUpdateEventHandler) eventService.getHandlers("USER_UPDATED").toArray()[0];
        userPasswordHandler.cleanUserMap();
        return userPasswordHandler;
    }

}
