/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.impl.IdentityServiceImpl;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IdentityServiceImplForUserTest {

    private Recorder recorder;

    private ReadPersistenceService persistence;

    private EventService eventService;

    private TechnicalLoggerService logger;

    private IdentityServiceImpl identityServiceImpl;

    @Before
    public void setup() {
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        eventService = mock(EventService.class);
        logger = mock(TechnicalLoggerService.class, new Returns(true));
        identityServiceImpl = new IdentityServiceImpl(persistence, recorder, eventService, null, logger, null, null);
    }

    @Test
    public void getNumberOfUsers() throws Exception {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(1L);
        Assert.assertEquals(1L, identityServiceImpl.getNumberOfUsers());

        verifyZeroInteractions(recorder);
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersWithException() throws Exception {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsers();
    }

    @Test
    public void getNumberOfUsersWithQueryOptions() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(persistence.getNumberOfEntities(SUser.class, options, null)).thenReturn(1L);
        Assert.assertEquals(1L, identityServiceImpl.getNumberOfUsers(options));
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfUsersWithQueryOptionsWithException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(persistence.getNumberOfEntities(SUser.class, options, null)).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsers(options);
    }

    @Test
    public void getNumberOfUsersByGroup() throws Exception {
        final long groupId = 1;

        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfUsersByGroup(groupId))).thenReturn(3L);
        Assert.assertEquals(3L, identityServiceImpl.getNumberOfUsersByGroup(groupId));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersByGroupWithException() throws Exception {
        final long groupId = 1;

        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfUsersByGroup(groupId))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsersByGroup(groupId);
    }

    @Test
    public void getNumberOfUsersByMembership() throws Exception {
        final long groupId = 1;
        final long roleId = 2;

        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfUsersByMembership(groupId, roleId))).thenReturn(3L);
        Assert.assertEquals(3L, identityServiceImpl.getNumberOfUsersByMembership(groupId, roleId));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersByMembershipWithException() throws Exception {
        final long groupId = 1;
        final long roleId = 2;

        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfUsersByMembership(groupId, roleId))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsersByMembership(groupId, roleId);
    }

    @Test
    public void getNumberOfUsersByRole() throws Exception {
        final long roleId = 2;

        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfUsersByRole(roleId))).thenReturn(3L);
        Assert.assertEquals(3L, identityServiceImpl.getNumberOfUsersByRole(roleId));
    }

    @Test(expected = SIdentityException.class)
    public void getNumberOfUsersByRoleWithException() throws Exception {
        final long roleId = 2;

        when(persistence.selectOne(SelectDescriptorBuilder.getNumberOfUsersByRole(roleId))).thenThrow(new SBonitaReadException(""));
        identityServiceImpl.getNumberOfUsersByRole(roleId);
    }

    @Test
    public void getUser() throws Exception {
        final long userId = 1;

        final SUser sUser = mock(SUser.class);
        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId))).thenReturn(sUser);
        Assert.assertEquals(sUser, identityServiceImpl.getUser(userId));
    }

    @Test
    public void getUsers() throws Exception {
        final SUser sUser1 = mock(SUser.class);
        final SUser sUser2 = mock(SUser.class);
        final SUser sUser3 = mock(SUser.class);
        final List<SUser> users = Arrays.asList(sUser1, sUser2, sUser3);
        final List<Long> ids = Arrays.asList(1l, 2l, 3l);
        when(persistence.selectList(SelectDescriptorBuilder.getElementsByIds(SUser.class, "User", ids))).thenReturn(users);
        Assert.assertEquals(users, identityServiceImpl.getUsers(ids));
    }

    @Test
    public void getUsersNullIds() throws Exception {
        assertTrue(identityServiceImpl.getUsers(null).isEmpty());
    }

    @Test
    public void getUsersEmptyIds() throws Exception {
        assertTrue(identityServiceImpl.getUsers(Collections.<Long> emptyList()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SUserNotFoundException.class)
    public void getUsersThrowException() throws Exception {
        when(persistence.selectList(SelectDescriptorBuilder.getElementsByIds(SUser.class, "User", any(Collection.class))))
                .thenThrow(SBonitaReadException.class);
        assertTrue(identityServiceImpl.getUsers(Arrays.asList(1l)).isEmpty());
    }

    @Test(expected = SUserNotFoundException.class)
    public void getUserWithReadException() throws Exception {
        final long userId = 1;

        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId))).thenThrow(new SBonitaReadException("Brinnggg !"));
        identityServiceImpl.getUser(userId);
    }

    @Test(expected = SUserNotFoundException.class)
    public void getUserWithWrongId() throws Exception {
        final long userId = 1;

        when(persistence.selectById(SelectDescriptorBuilder.getElementById(SUser.class, "User", userId))).thenReturn(null);
        identityServiceImpl.getUser(userId);
    }

    @Test
    public void searchUsers() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);
        final SUser user = mock(SUser.class);
        when(persistence.searchEntity(SUser.class, options, null)).thenReturn(Collections.singletonList(user));

        assertEquals(user, identityServiceImpl.searchUsers(options).get(0));
    }

    // @Test
    // public void createUserWithUserImpl() throws Exception {
    // final IdentityServiceImpl identityServiceImpl = new IdentityServiceImpl(persistence, recorder, eventService, new IdentityModelBuilderImpl(), logger,
    // queriableLoggerService, credentialsEncrypter);
    //
    // SUser baseUser = new SUserImpl();
    //
    // when(credentialsEncrypter.hash(anyString())).thenReturn("hashedPassword");
    // SUserImpl userFromRecorder = new SUserImpl();
    // userFromRecorder.setId(123456789l);
    // when(recorder.recordInsert(any(InsertRecord.class), any(SInsertEvent.class))).thenReturn(userFromRecorder);
    // final SUser returnedUser = identityServiceImpl.createUser(baseUser);
    // assertEquals("hashedPassword", returnedUser.getPassword());
    // }

    // public void createUser() throws Exception {
    // final SUser sUser = buildEnabledSUser("firstname", "lastname", "pwd", 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // final SUser result = identityService.createUser(sUser);
    // tx.complete();
    // assertEquals(sUser.getCreatedBy(), result.getCreatedBy());
    // assertEquals(sUser.getCreationDate(), result.getCreationDate());
    // assertEquals(sUser.getDelegeeUserName(), result.getDelegeeUserName());
    // assertEquals(sUser.isEnabled(), result.isEnabled());
    // assertEquals(sUser.getFirstName(), result.getFirstName());
    // assertEquals(sUser.getIconName(), result.getIconName());
    // assertEquals(sUser.getIconPath(), result.getIconPath());
    // assertEquals(sUser.getJobTitle(), result.getJobTitle());
    // assertEquals(sUser.getLastConnection(), result.getLastConnection());
    // assertEquals(sUser.getLastName(), result.getLastName());
    // assertEquals(sUser.getLastUpdate(), result.getLastUpdate());
    // assertEquals(sUser.getManagerUserId(), result.getManagerUserId());
    // // assertEquals(sUser.getPassword(), result.getPassword());
    // assertEquals(sUser.getTitle(), result.getTitle());
    // assertEquals(sUser.getUserName(), result.getUserName());
    //
    // // clean-up
    // deleteSUser(result);
    // }

    // public void chechCredentials() throws Exception {
    // final SUser sUser = createEnabledSUser("firstname", "lastname", "pwd", 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // final boolean result = identityService.chechCredentials(sUser, "pwd");
    // tx.complete();
    // assertTrue(result);
    //
    // // clean-up
    // deleteSUser(sUser);
    // }

    // public void chechCredentialsWithWrongPassword() throws Exception {
    // final SUser sUser = createEnabledSUser("firstname", "lastname", "pwd", 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // final boolean result = identityService.chechCredentials(sUser, "plop");
    // tx.complete();
    // assertFalse(result);
    //
    // // clean-up
    // deleteSUser(sUser);
    // }

    // @Test(expected = STransactionPrepareException.class)
    // public void cannotCreateTwoUserWithTheSameUserName() throws Exception {
    // final SUser sUser1 = buildEnabledSUser("firstname", "lastname", "pwd", 0);
    // final SUser sUser2 = buildEnabledSUser("firstname", "lastname", "pwd", 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // identityService.createUser(sUser1);
    // try {
    // identityService.createUser(sUser2);
    // } finally {
    // tx.complete();
    // }
    // }

    // @Test(expected = STransactionPrepareException.class)
    // public void cannotCreateAUserWithANullUserName() throws Exception {
    // final SUser sUser = buildEnabledSUser(null, "lastname", "pwd", 0);
    //
    // tx.begin();
    // try {
    // identityService.createUser(sUser);
    // } finally {
    // tx.complete();
    // }
    // }

    // @Test(expected = NullPointerException.class)
    // public void cannotCreateAUserWithANullPassword() throws Exception {
    // final SUser sUser = buildEnabledSUser("firstname", "lastname", null, 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // try {
    // identityService.createUser(sUser);
    // } finally {
    // tx.complete();
    // }
    // }

    // @Cover(classes = { IdentityService.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Create", "Get", "UserContactInfo" })
    // @Test
    // public void createAndGetUserContactInfo() throws Exception {
    // final SUser sUser = createEnabledSUser("firstname", "lastname", "pwd", 0);
    // final SContactInfo sContactInfo = buildSContactInfo(sUser.getId(), true);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // identityService.createUserContactInfo(sContactInfo);
    //
    // final SContactInfo result = identityService.getUserContactInfo(sUser.getId(), true);
    // tx.complete();
    // assertEquals(sContactInfo.getAddress(), result.getAddress());
    // assertEquals(sContactInfo.getBuilding(), result.getBuilding());
    // assertEquals(sContactInfo.getCity(), result.getCity());
    // assertEquals(sContactInfo.getCountry(), result.getCountry());
    // assertEquals(sContactInfo.getDiscriminator(), result.getDiscriminator());
    // assertEquals(sContactInfo.getEmail(), result.getEmail());
    // assertEquals(sContactInfo.getFaxNumber(), result.getFaxNumber());
    // assertEquals(sContactInfo.getMobileNumber(), result.getMobileNumber());
    // assertEquals(sContactInfo.getPhoneNumber(), result.getPhoneNumber());
    // assertEquals(sContactInfo.getRoom(), result.getRoom());
    // assertEquals(sContactInfo.getState(), result.getState());
    // assertEquals(sContactInfo.getUserId(), result.getUserId());
    // assertEquals(sContactInfo.getWebsite(), result.getWebsite());
    // assertEquals(sContactInfo.getZipCode(), result.getZipCode());
    //
    // // clean-up
    // // deleteSUser(sContactInfo);
    // deleteSUser(sUser);
    // }

    // @Test(expected = SUserNotFoundException.class)
    // public void getUserByUsernameWithException() throws Exception {
    // final SUser sUser = createEnabledSUser("firstname", "lastname", "pwd", 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // try {
    // identityService.getUserByUserName("blabla");
    // } finally {
    // tx.complete();
    //
    // // clean-up
    // deleteSUser(sUser);
    // }
    // }

    // public void getUserByUsername() throws Exception {
    // final SUser sUser = createEnabledSUser("firstname", "lastname", "pwd", 0);
    //
    // final BusinessTransaction tx = bpmServicesBuilder.getTransactionService().createTransaction();
    // tx.begin();
    // final SUser result = identityService.getUserByUserName("firstname");
    // tx.complete();
    // assertEquals(sUser.getUserName(), result.getUserName());
    //
    // // clean-up
    // deleteSUser(sUser);
    // }
}
