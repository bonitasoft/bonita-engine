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
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.impl.IconImpl;
import org.bonitasoft.engine.platform.NodeNotStartedException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UserIT extends TestWithTechnicalUser {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * This test is here for arbitrary reason: it has to be tested on ANY API call.
     */
    @Test(expected = NodeNotStartedException.class)
    public void unableToCallPlatformMethodOnStoppedNode() throws Exception {
        logoutOnTenant();
        PlatformSession session = loginOnPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.stopNode();
        logoutOnPlatform(session);
        try {
            loginOnDefaultTenantWithDefaultTechnicalUser();
        } finally {
            session = loginOnPlatform();
            platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.startNode();
            logoutOnPlatform(session);
            loginOnDefaultTenantWithDefaultTechnicalUser();
        }
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyPassword() throws BonitaException {
        final String userName = "matti";
        final String password = "";
        createUser(userName, password);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyUserName() throws BonitaException {
        final String userName = "";
        final String password = "revontuli";
        createUser(userName, password);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullPassword() throws BonitaException {
        final String userName = "matti";
        createUser(userName, null);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullUserName() throws BonitaException {
        final String password = "revontuli";
        createUser(null, password);
    }

    @Test
    public void createUserByUsernameAndPassword() throws BonitaException {
        getIdentityAPI().getNumberOfUsers();
        final User userCreated = getIdentityAPI().createUser("bonitasoft", "123456");
        assertNotNull(userCreated);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        final User user = getIdentityAPI().getUserByUserName("bonitasoft");
        assertNotNull(user);
        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test
    public void createEnabledUserByUsernameAndPassword() throws BonitaException {
        final User userCreated = getIdentityAPI().createUser("bonitasoft", "123456");
        assertNotNull(userCreated);

        final User user = getIdentityAPI().getUserByUserName("bonitasoft");
        assertNotNull(user);
        assertTrue(user.isEnabled());

        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test(expected = AlreadyExistsException.class)
    public void createUserByUsernameAndPasswordException() throws BonitaException {
        final User userCreated = getIdentityAPI().createUser("bonitasoft", "123456");
        assertNotNull(userCreated);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        final User user = getIdentityAPI().getUserByUserName("bonitasoft");
        assertNotNull(user);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        try {
            getIdentityAPI().createUser("bonitasoft", "123456");
        } finally {
            getIdentityAPI().deleteUser("bonitasoft");
        }
    }

    public void getFirstPageWithNoResult() {
        getIdentityAPI().getUsers(0, 10, UserCriterion.USER_NAME_ASC);
    }

    @Test(expected = CreationException.class)
    public void createUserFailed() throws BonitaException {
        getIdentityAPI().createUser(null, null);
    }

    @Test(expected = CreationException.class)
    public void createUserUsingNullUser() throws BonitaException {
        getIdentityAPI().createUser(null);
    }

    @Test
    public void createUserByAUser() throws BonitaException {
        final String userName = "spring";
        final UserCreator creator = new UserCreator(userName, "bpm");
        creator.setTitle("wwwwwwwwwwwwwwwwwwwwwwwwwwww");
        final User user = getIdentityAPI().createUser(creator);
        assertNotNull(user);
        assertEquals(userName, user.getUserName());

        getIdentityAPI().deleteUser(userName);
    }

    @Test
    public void createEnabledUserByAUser() throws BonitaException {
        final User user = getIdentityAPI().createUser("bonitasoft", "bpm");
        assertNotNull(user);
        assertTrue(user.isEnabled());

        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test
    public void createDisabledUserByAUser() throws BonitaException {
        final UserCreator creator = new UserCreator("bonitasoft", "bpm");
        creator.setEnabled(false);
        final User user = getIdentityAPI().createUser(creator);
        assertNotNull(user);
        assertFalse(user.isEnabled());

        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullUserNameUsingBuilder() throws BonitaException {
        getIdentityAPI().createUser(null, "revontuli");
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullPasswordUsingBuilder() throws BonitaException {
        getIdentityAPI().createUser("matti", null);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyUserNameUsingBuilder() throws BonitaException {
        getIdentityAPI().createUser("", "revontuli");
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyPasswordUsingBuilder() throws BonitaException {
        getIdentityAPI().createUser("matti", "");
    }

    @Test(expected = UserNotFoundException.class)
    public void getUserByUsernameWithException() throws BonitaException {
        getIdentityAPI().getUserByUserName("Bonita");
    }

    @Test
    public void getUserByUsername() throws BonitaException {
        getIdentityAPI().createUser("bonita", "password");
        final User user = getIdentityAPI().getUserByUserName("bonita");
        assertEquals("bonita", user.getUserName());
        assertNotSame("password", user.getPassword());

        getIdentityAPI().deleteUser("bonita");
    }

    @Test
    public void getNumberOfUsers() throws BonitaException {
        getIdentityAPI().createUser("jane", "bpm");
        getIdentityAPI().createUser("paul", "bpm");

        final long usersCount = getIdentityAPI().getNumberOfUsers();
        assertEquals(2, usersCount);

        getIdentityAPI().deleteUser("jane");
        getIdentityAPI().deleteUser("paul");
    }

    @Test
    public void getUserById() throws BonitaException {
        final User userCreated = getIdentityAPI().createUser("zhang", "engine");
        final User user = getIdentityAPI().getUser(userCreated.getId());
        assertNotNull(user);
        assertEquals("zhang", user.getUserName());
        assertNotSame("engine", user.getPassword());

        getIdentityAPI().deleteUser("zhang");
    }

    @Test(expected = UserNotFoundException.class)
    public void cannotGetTechUser() throws BonitaException {
        getIdentityAPI().getUser(-1);
    }

    @Test
    public void cannotGetTechUserInList() {
        final Map<Long, User> users = getIdentityAPI().getUsers(Arrays.asList(-1L));
        assertNull(users.get(-1));
    }

    @Test(expected = UserNotFoundException.class)
    public void getUserByIDWithUserNotFoundException() throws BonitaException {
        final User userCreated = getIdentityAPI().createUser("zhang", "engine");
        try {
            getIdentityAPI().getUser(userCreated.getId() + 100);
        } finally {
            getIdentityAPI().deleteUser("zhang");
        }
    }

    @Test
    public void getUsersByIDs() throws BonitaException {
        final User userCreated1 = getIdentityAPI().createUser("zhang", "engine");
        final User userCreated2 = getIdentityAPI().createUser("jmege", "engine");

        final List<Long> userIds = new ArrayList<>();
        userIds.add(userCreated1.getId());
        userIds.add(userCreated2.getId());

        final Map<Long, User> users = getIdentityAPI().getUsers(userIds);
        assertNotNull(users);
        assertEquals(2, users.size());

        assertEquals("zhang", users.get(userCreated1.getId()).getUserName());
        assertNotSame("engine", users.get(userCreated1.getId()).getPassword());

        assertEquals("jmege", users.get(userCreated2.getId()).getUserName());
        assertNotSame("engine", users.get(userCreated2.getId()).getPassword());

        deleteUsers(userCreated1, userCreated2);
    }

    public void getUsersByIDsWithoutUserNotFoundException() throws BonitaException {
        final User userCreated1 = getIdentityAPI().createUser("zhang", "engine");
        final User userCreated2 = getIdentityAPI().createUser("jmege", "engine");

        final List<Long> userIds = new ArrayList<>();
        userIds.add(userCreated1.getId());
        userIds.add(userCreated2.getId() + 100);

        final Map<Long, User> users = getIdentityAPI().getUsers(userIds);
        assertNotNull(users);
        assertEquals(1, users.size());

        assertEquals("zhang", users.get(userCreated1.getId()).getUserName());
        assertEquals("engine", users.get(userCreated1.getId()).getPassword());

        deleteUsers(userCreated1, userCreated2);
    }

    @Test
    public void deleteUserByUserName() throws BonitaException {
        getIdentityAPI().createUser("testDelete", "engine");
        getIdentityAPI().deleteUser("testDelete");
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
    }

    @Test
    public void deleteNonExistingUserByUserName() throws BonitaException {
        final String userName = "testDelete";
        getIdentityAPI().createUser(userName, "engine");
        getIdentityAPI().deleteUser(userName);
        getIdentityAPI().deleteUser(userName);
    }

    @Test(expected = DeletionException.class)
    public void deleteUserByUserNameWithUserDeletionException() throws BonitaException {
        getIdentityAPI().deleteUser(null);
    }

    @Test
    public void deleteUser() throws BonitaException {
        final User user = getIdentityAPI().createUser("testDelete", "engine");
        getIdentityAPI().deleteUser(user.getId());
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
    }

    @Test
    public void testUserContactInfos() throws BonitaException {
        final UserCreator creator = new UserCreator("john", "bpm");
        creator.setFirstName("John").setLastName("Doe");
        final ContactDataCreator persoCreator = new ContactDataCreator();
        // BS-7711
        persoCreator.setEmail("anemailwithmorethanfifteencharacter@extremellylongdomainname.com");
        final ContactDataCreator proCreator = new ContactDataCreator();
        proCreator.setEmail("john.doe@bonitasoft.com");
        creator.setPersonalContactData(persoCreator);
        creator.setProfessionalContactData(proCreator);
        final User john = getIdentityAPI().createUser(creator);

        final ContactData persoData = getIdentityAPI().getUserContactData(john.getId(), true);
        assertEquals("anemailwithmorethanfifteencharacter@extremellylongdomainname.com", persoData.getEmail());
        final ContactData prooData = getIdentityAPI().getUserContactData(john.getId(), false);
        assertEquals("john.doe@bonitasoft.com", prooData.getEmail());

        getIdentityAPI().deleteUser(john.getId());
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
        assertNull(getIdentityAPI().getUserContactData(john.getId(), true));
    }

    @Test
    public void deleteNonExistingUser() throws BonitaException {
        final User user = getIdentityAPI().createUser("testDelete", "engine");
        getIdentityAPI().deleteUser(user.getId());
        getIdentityAPI().deleteUser(user.getId());
    }

    @Test
    public void deleteUsers() throws BonitaException {
        final List<Long> userIds = new ArrayList<>();
        userIds.add(getIdentityAPI().createUser("user1", "engine").getId());
        userIds.add(getIdentityAPI().createUser("user2", "engine").getId());
        userIds.add(getIdentityAPI().createUser("user3", "engine").getId());
        userIds.add(getIdentityAPI().createUser("user4", "engine").getId());
        userIds.add(getIdentityAPI().createUser("user5", "engine").getId());

        getIdentityAPI().deleteUsers(userIds);
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
    }

    @Test
    public void deleteNonExistingUsers() throws BonitaException {
        getIdentityAPI().deleteUsers(null);
    }

    @Test
    public void deleteUsersDeleteAllExistingOnesAndIgnoresOthers() throws BonitaException {
        final List<Long> userIds = new ArrayList<>();
        userIds.add(getIdentityAPI().createUser("user1", "engine").getId());
        userIds.add(getIdentityAPI().createUser("user2", "engine").getId());
        userIds.add(152458L);

        getIdentityAPI().deleteUsers(userIds);
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
    }

    @Test
    public void updateUser() throws BonitaException {
        final String james = "james";
        final User user = getIdentityAPI().createUser(james, "mbp");

        final UserUpdater updateDescriptor = buildUserUpdaterWithProAndPersoContact();
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);

        checkUser(updatedUser);

        final ContactData persoData = getIdentityAPI().getUserContactData(updatedUser.getId(), true);
        checkPersoUserContactData(persoData);

        final ContactData proData = getIdentityAPI().getUserContactData(updatedUser.getId(), false);
        checkProUserContactData(proData);

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    private UserUpdater buildUserUpdater() {
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setFirstName("Changed first name");
        updateDescriptor.setIconName("new icon name");
        updateDescriptor.setIconPath("new_icon_path");
        updateDescriptor.setJobTitle("New job title");
        updateDescriptor.setLastName("Modified Last name");
        updateDescriptor.setManagerId(12354L);
        updateDescriptor.setPassword("Ch4n63D_P455W0RD");
        updateDescriptor.setUserName("new_user_name");
        updateDescriptor.setTitle("titre");
        return updateDescriptor;
    }

    private void checkUser(final User updatedUser) {
        assertNotNull(updatedUser);
        assertEquals("new_user_name", updatedUser.getUserName());
        assertEquals("Changed first name", updatedUser.getFirstName());
        assertEquals("New job title", updatedUser.getJobTitle());
        assertEquals("Modified Last name", updatedUser.getLastName());
        assertEquals(12354L, updatedUser.getManagerUserId());
        assertNotSame("Ch4n63D_P455W0RD", updatedUser.getPassword());
        assertEquals("titre", updatedUser.getTitle());
    }

    private UserUpdater buildUserUpdaterWithProContact() {
        final ContactDataUpdater proDataUpDescr = buildProContactDataUpdater();
        final UserUpdater updateDescriptor = buildUserUpdater();
        updateDescriptor.setProfessionalContactData(proDataUpDescr);
        return updateDescriptor;
    }

    private UserUpdater buildUserUpdaterWithProAndPersoContact() {
        final ContactDataUpdater proDataUpDescr = buildProContactDataUpdater();
        final ContactDataUpdater persoDataUpDescr = buildPersoContactDataUpdater();
        final UserUpdater updateDescriptor = buildUserUpdater();
        updateDescriptor.setProfessionalContactData(proDataUpDescr);
        updateDescriptor.setPersonalContactData(persoDataUpDescr);
        return updateDescriptor;
    }

    private ContactDataUpdater buildProContactDataUpdater() {
        final ContactDataUpdater proDataUpDescr = new ContactDataUpdater();
        proDataUpDescr.setAddress("34 Gustave Eiffel");
        proDataUpDescr.setBuilding("BigBlock");
        proDataUpDescr.setCity("L.A.");
        proDataUpDescr.setCountry("Spain");
        proDataUpDescr.setEmail("noreply@bonitasoft.com");
        proDataUpDescr.setFaxNumber("01-356743254");
        proDataUpDescr.setMobileNumber("06-02-1111111");
        proDataUpDescr.setPhoneNumber("04-76-111111");
        proDataUpDescr.setRoom("A304");
        proDataUpDescr.setState("Isere");
        proDataUpDescr.setWebsite("http://www.bonitasoft.com");
        proDataUpDescr.setZipCode("38000");
        return proDataUpDescr;
    }

    private void checkProUserContactData(final ContactData proData) {
        assertEquals("34 Gustave Eiffel", proData.getAddress());
        assertEquals("BigBlock", proData.getBuilding());
        assertEquals("L.A.", proData.getCity());
        assertEquals("Spain", proData.getCountry());
        assertEquals("noreply@bonitasoft.com", proData.getEmail());
        assertEquals("01-356743254", proData.getFaxNumber());
        assertEquals("06-02-1111111", proData.getMobileNumber());
        assertEquals("04-76-111111", proData.getPhoneNumber());
        assertEquals("A304", proData.getRoom());
        assertEquals("Isere", proData.getState());
        assertEquals("http://www.bonitasoft.com", proData.getWebsite());
        assertEquals("38000", proData.getZipCode());
    }

    private ContactDataUpdater buildPersoContactDataUpdater() {
        final ContactDataUpdater persoDataUpDescr = new ContactDataUpdater();
        persoDataUpDescr.setAddress("3 rue des lilas");
        persoDataUpDescr.setBuilding("SkyScrapper");
        persoDataUpDescr.setCity("Lyon");
        persoDataUpDescr.setCountry("Lichtenstein");
        persoDataUpDescr.setEmail("noreply@yahoo.es");
        persoDataUpDescr.setFaxNumber("01-020013021452");
        persoDataUpDescr.setMobileNumber("06-02-000000");
        persoDataUpDescr.setPhoneNumber("04-76-000000");
        persoDataUpDescr.setRoom("Home");
        persoDataUpDescr.setState("Rhône");
        persoDataUpDescr.setWebsite("http://perso.bonitasoft.com");
        persoDataUpDescr.setZipCode("69000");
        return persoDataUpDescr;
    }

    private void checkPersoUserContactData(final ContactData persoData) {
        assertEquals("3 rue des lilas", persoData.getAddress());
        assertEquals("SkyScrapper", persoData.getBuilding());
        assertEquals("Lyon", persoData.getCity());
        assertEquals("Lichtenstein", persoData.getCountry());
        assertEquals("noreply@yahoo.es", persoData.getEmail());
        assertEquals("01-020013021452", persoData.getFaxNumber());
        assertEquals("06-02-000000", persoData.getMobileNumber());
        assertEquals("04-76-000000", persoData.getPhoneNumber());
        assertEquals("Home", persoData.getRoom());
        assertEquals("Rhône", persoData.getState());
        assertEquals("http://perso.bonitasoft.com", persoData.getWebsite());
        assertEquals("69000", persoData.getZipCode());
    }

    @Test
    public void updateUserToBeEnabled() throws BonitaException {
        // Create user, and updateDescriptor
        final User user = getIdentityAPI().createUser("bonitasoft", "123456");
        assertNotNull(user);
        assertTrue(user.isEnabled());
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(true);

        // Update user
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);
        assertNotNull(updatedUser);
        assertTrue(updatedUser.isEnabled());

        // Clean
        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test
    public void updateUserToBeDisabled() throws BonitaException {
        // Create user, and updateDescriptor
        final User user = getIdentityAPI().createUser("bonitasoft", "123456");
        assertNotNull(user);
        assertTrue(user.isEnabled());
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(false);

        // Update user
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);
        assertFalse(updatedUser.isEnabled());

        // Clean
        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test
    public void updateUserManager() throws BonitaException {
        final User matti = getIdentityAPI().createUser("matti", "bpm");
        final User james = getIdentityAPI().createUser("james", "bpm");
        assertEquals(0, james.getManagerUserId());
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setManagerId(matti.getId());
        final User updatedUser = getIdentityAPI().updateUser(james.getId(), updateDescriptor);
        assertNotNull(updatedUser);
        assertEquals(matti.getId(), updatedUser.getManagerUserId());

        deleteUsers(james, matti);
    }

    @Test(expected = UserNotFoundException.class)
    public void updateUserWithUserNotFoundException() throws BonitaException {
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setUserName("john");
        updateDescriptor.setPassword("bpm");
        final int userId = 100;
        getIdentityAPI().updateUser(userId, updateDescriptor);
    }

    @Test(expected = UpdateException.class)
    public void updateUserWithUserUpdateException() throws BonitaException {
        final User oldUser = getIdentityAPI().createUser("old", "oldPassword");
        try {
            getIdentityAPI().updateUser(oldUser.getId(), null);
        } finally {
            getIdentityAPI().deleteUser(oldUser.getId());
        }
    }

    @Test
    public void updateUserWithOnlyDataChanging() throws BonitaException {
        final User user = getIdentityAPI().createUser("james", "mbp");

        final ContactDataUpdater persoDataUpDescr = buildPersoContactDataUpdater();
        final ContactDataUpdater proDataUpDescr = buildProContactDataUpdater();

        final UserUpdater updater = new UserUpdater();
        updater.setPersonalContactData(persoDataUpDescr);
        updater.setProfessionalContactData(proDataUpDescr);
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updater);
        assertNotNull(updatedUser);

        final ContactData persoData = getIdentityAPI().getUserContactData(updatedUser.getId(), true);
        checkPersoUserContactData(persoData);

        final ContactData proData = getIdentityAPI().getUserContactData(updatedUser.getId(), false);
        checkProUserContactData(proData);

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    @Test
    public void getPaginatedUsersWithUserCriterion() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("auser1o", "bpm", "a", "a");
        final User user2 = getIdentityAPI().createUser("cuser2o", "bpm", "c", "c");
        final User user3 = getIdentityAPI().createUser("buser3o", "bpm", "b", "b");
        final User user4 = getIdentityAPI().createUser("euser4o", "bpm", "e", "e");
        final User user5 = getIdentityAPI().createUser("duser5o", "bpm", "d", "d");

        List<User> usersASC = getIdentityAPI().getUsers(0, 3, UserCriterion.USER_NAME_ASC);
        assertEquals(3, usersASC.size());
        assertEquals("auser1o", usersASC.get(0).getUserName());
        assertEquals("buser3o", usersASC.get(1).getUserName());
        assertEquals("cuser2o", usersASC.get(2).getUserName());
        usersASC = getIdentityAPI().getUsers(3, 3, UserCriterion.USER_NAME_ASC);
        assertEquals(2, usersASC.size());
        assertEquals("duser5o", usersASC.get(0).getUserName());
        assertEquals("euser4o", usersASC.get(1).getUserName());
        final List<User> users = getIdentityAPI().getUsers(6, 3, UserCriterion.USER_NAME_ASC);
        assertTrue(users.isEmpty());
        final List<User> usersDESC = getIdentityAPI().getUsers(0, 3, UserCriterion.USER_NAME_DESC);
        assertEquals(3, usersDESC.size());
        assertEquals("euser4o", usersDESC.get(0).getUserName());
        assertEquals("duser5o", usersDESC.get(1).getUserName());
        assertEquals("cuser2o", usersDESC.get(2).getUserName());

        final List<User> usersFirstNameASC = getIdentityAPI().getUsers(0, 3, UserCriterion.FIRST_NAME_ASC);
        assertEquals(3, usersFirstNameASC.size());
        assertEquals("a", usersFirstNameASC.get(0).getFirstName());
        assertEquals("b", usersFirstNameASC.get(1).getFirstName());
        assertEquals("c", usersFirstNameASC.get(2).getFirstName());

        final List<User> usersFirstNameDESC = getIdentityAPI().getUsers(0, 3, UserCriterion.FIRST_NAME_DESC);
        assertEquals(3, usersFirstNameDESC.size());
        assertEquals("e", usersFirstNameDESC.get(0).getFirstName());
        assertEquals("d", usersFirstNameDESC.get(1).getFirstName());
        assertEquals("c", usersFirstNameDESC.get(2).getFirstName());

        final List<User> usersLastNameASC = getIdentityAPI().getUsers(0, 3, UserCriterion.LAST_NAME_ASC);
        assertEquals(3, usersLastNameASC.size());
        assertEquals("a", usersLastNameASC.get(0).getLastName());
        assertEquals("b", usersLastNameASC.get(1).getLastName());
        assertEquals("c", usersLastNameASC.get(2).getLastName());

        final List<User> usersLastNameDESC = getIdentityAPI().getUsers(0, 3, UserCriterion.LAST_NAME_DESC);
        assertEquals(3, usersLastNameDESC.size());
        assertEquals("e", usersLastNameDESC.get(0).getLastName());
        assertEquals("d", usersLastNameDESC.get(1).getLastName());
        assertEquals("c", usersLastNameDESC.get(2).getLastName());

        deleteUsers(user1, user2, user3, user4, user5);
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotCreateTwoUserWithTheSameUserName() throws BonitaException {
        final String username = "install";

        final User user1 = getIdentityAPI().createUser(username, "bpm");
        try {
            getIdentityAPI().createUser(username, "bos");
        } finally {
            getIdentityAPI().deleteUser(user1.getId());
        }
    }

    @Test(expected = SearchException.class)
    public void searchUserWithWrongSortKey() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.FIRST_NAME, "Jean-Gustave");
        builder.sort("WRONG_SORT_KEY", Order.ASC);
        getIdentityAPI().searchUsers(builder.done());
    }

    @Test
    public void searchUser() throws BonitaException {
        final List<User> users = new ArrayList<>();
        users.add(getIdentityAPI().createUser("jgrGF[|00", "bpm", "John", "Taylor"));
        users.add(getIdentityAPI().createUser("45èDG'fgb", "bpm", "Jack", "Jack"));
        users.add(getIdentityAPI().createUser("à\"(èg", "bpm", "John", "Smith"));
        users.add(getIdentityAPI().createUser("^^jhg", "bpm", "Paul", "Taylor"));
        users.add(getIdentityAPI().createUser("مرحبا!", "bpm", "Jack", "Jack"));
        users.add(getIdentityAPI().createUser("user02", "bpm", "Pierre", "Smith"));
        users.add(getIdentityAPI().createUser("User00", "bpm", "Marie", "Taylor"));

        // Filter and order test
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.FIRST_NAME, "John");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        List<User> usersResult = searchUsers.getResult();
        assertEquals(users.get(2), usersResult.get(0));
        assertEquals(users.get(0), usersResult.get(1));

        // Pagination test
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        final List<User> allUsers = getIdentityAPI().searchUsers(builder.done()).getResult();
        assertEquals(7, allUsers.size());

        builder = new SearchOptionsBuilder(0, 5);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        usersResult = getIdentityAPI().searchUsers(builder.done()).getResult();
        assertEquals(5, usersResult.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(allUsers.get(i), usersResult.get(i));
        }

        builder = new SearchOptionsBuilder(5, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        usersResult = getIdentityAPI().searchUsers(builder.done()).getResult();
        assertEquals(2, usersResult.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(allUsers.get(i + 5), usersResult.get(i));
        }

        deleteUsers(users);
    }

    @Test
    public void searchUserSortedById() throws BonitaException {
        final List<User> users = new ArrayList<>();
        users.add(getIdentityAPI().createUser("jgrGF[|00", "bpm", "John", "Taylor"));
        users.add(getIdentityAPI().createUser("user02", "bpm", "Pierre", "Smith"));
        users.add(getIdentityAPI().createUser("User00", "bpm", "Marie", "Taylor"));

        // Search ordered by id
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.ID, Order.ASC);
        List<User> usersResult = getIdentityAPI().searchUsers(builder.done()).getResult();

        assertEquals(3, usersResult.size());
        assertThat(usersResult.get(0).getId()).isLessThan(usersResult.get(1).getId());
        assertThat(usersResult.get(1).getId()).isLessThan(usersResult.get(2).getId());

        deleteUsers(users);
    }

    @Test
    public void searchEnabledDisabledUsers() throws BonitaException {
        // Create users
        final User john = getIdentityAPI().createUser("john002", "bpm", "John", "Taylor");
        final User jack = getIdentityAPI().createUser("jack001", "bpm", "Jack", "Doe");

        // Disabled jack
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(false);
        final User updatedJack = getIdentityAPI().updateUser(jack.getId(), updateDescriptor);

        // Search enabled users
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.ENABLED, true);
        SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        List<User> users = searchUsers.getResult();
        assertEquals(john, users.get(0));

        // Search disabled users
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.ENABLED, false);
        searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        users = searchUsers.getResult();
        assertEquals(updatedJack, users.get(0));

        // Clean up
        deleteUsers(john, jack);
    }

    @Test
    public void searchUserUsingTerm() throws BonitaException {
        final User john2 = getIdentityAPI().createUser("john002", "bpm", "John", "Taylor");
        final User jack = getIdentityAPI().createUser("jack001", "bpm", "Jack", "Doe");
        final User john1 = getIdentityAPI().createUser("john001", "bpm", "John", "Smith");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("Jo");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(john1, users.get(0));
        assertEquals(john2, users.get(1));

        deleteUsers(john1, john2, jack);
    }

    @Test
    public void searchUserWithApostrophe() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("'john'002", "bpm", "John", "A");
        final User user3 = getIdentityAPI().createUser("c", "bpm", "'john'n", "C");
        final User user4 = getIdentityAPI().createUser("d", "bpm", "'d", "John'");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("'");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(3, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(user4, users.get(0));
        assertEquals(user3, users.get(1));
        assertEquals(user1, users.get(2));

        deleteUsers(user1, user3, user4);
    }

    @Test
    public void searchTermWithSpecialChars() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("Séba");
        final SearchOptions searchOptions = builder.done();
        SearchResult<User> searchUsers = getIdentityAPI().searchUsers(searchOptions);
        assertEquals(0, searchUsers.getCount());

        final User friend = getIdentityAPI().createUser("Sébastien", "ENCRYPTED");

        searchUsers = getIdentityAPI().searchUsers(searchOptions);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(friend, users.get(0));

        getIdentityAPI().deleteUser(friend.getId());
    }

    @Test
    public void searchUsersInGroup() throws BonitaException {
        final User john1 = createUser("john001", "bpm", "John", "Smith");
        final User jack = createUser("jack001", "bpm", "Jack", "Doe");
        final User john2 = createUser("john002", "bpm", "John", "Taylor");

        final Group group = createGroup("group1");
        final Role role = createRole("manager");

        getIdentityAPI().addUserMemberships(Arrays.asList(john1.getId(), jack.getId()), group.getId(), role.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.GROUP_ID, (Long) (group.getId()));
        builder.sort(UserSearchDescriptor.USER_NAME, Order.DESC);
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(john1, users.get(0));
        assertEquals(jack, users.get(1));

        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
        deleteUsers(john1, john2, jack);
    }

    @Test
    public void searchUsersInRole() throws BonitaException {
        final User john1 = createUser("john001", "bpm", "John", "Smith");
        final User jack = createUser("jack001", "bpm", "Jack", "Doe");
        final User john2 = createUser("john002", "bpm", "John", "Taylor");

        final Group group = createGroup("group1");
        final Role role = createRole("manager");

        getIdentityAPI().addUserMemberships(Arrays.asList(john1.getId(), jack.getId()), group.getId(), role.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.ROLE_ID, (Long) (role.getId()));
        builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(jack, users.get(0));
        assertEquals(john1, users.get(1));

        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
        deleteUsers(john1, john2, jack);
    }

    @Test
    public void searchUsersInRoleAndGroup() throws BonitaException {
        final User john1 = createUser("john001", "bpm", "John", "Smith");
        final User jack = createUser("jack001", "bpm", "Jack", "Doe");
        final User john2 = createUser("john002", "bpm", "John", "Taylor");

        final Group group1 = createGroup("group1");
        final Group group2 = createGroup("group2");
        final Role role1 = createRole("manager");
        final Role role2 = createRole("delivery");

        getIdentityAPI().addUserMemberships(Arrays.asList(john1.getId(), jack.getId()), group1.getId(), role1.getId());
        getIdentityAPI().addUserMemberships(Arrays.asList(john2.getId()), group2.getId(), role1.getId());
        getIdentityAPI().addUserMemberships(Arrays.asList(john2.getId(), jack.getId()), group1.getId(), role2.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.GROUP_ID, group2.getId());
        builder.filter(UserSearchDescriptor.ROLE_ID, role1.getId());
        builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(john2, users.get(0));

        deleteGroups(group1, group2);
        deleteRoles(role1, role2);
        deleteUsers(john1, john2, jack);
    }

    @Test
    public void searchTeamMembers() throws BonitaException {
        final User manager = getIdentityAPI().createUser("john002", "bpm", "John", "Taylor");
        final UserCreator creator = new UserCreator("jack001", "bpm");
        creator.setFirstName("Jack").setLastName("Doe").setManagerUserId(manager.getId());
        final User jack = getIdentityAPI().createUser(creator);
        final User john = getIdentityAPI().createUser("john001", "bpm", "John", "Smith");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.MANAGER_USER_ID, (Long) manager.getId());
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(jack, users.get(0));

        deleteUsers(john, manager, jack);
    }

    @Test
    public void searchUsersNotInTeam() throws BonitaException {
        // Manager
        final User manager = getIdentityAPI().createUser("john002", "bpm", "John", "Taylor");

        // User with manager
        final UserCreator creator = new UserCreator("jack001", "bpm");
        creator.setFirstName("Jack").setLastName("Doe").setManagerUserId(manager.getId());
        final User jack = getIdentityAPI().createUser(creator);

        // User without manager
        final User john = getIdentityAPI().createUser("john003", "bpm", "John", "Smith");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.differentFrom(UserSearchDescriptor.MANAGER_USER_ID, manager.getId()).differentFrom(UserSearchDescriptor.USER_NAME, manager.getUserName());
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(john, users.get(0));

        deleteUsers(john, manager, jack);
    }

    @Test
    public void getUsersFromIdsShouldReturnUsersInTheRightOrder() throws BonitaException {
        final List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(getIdentityAPI().createUser("zhao", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("qian", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("sun", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("li", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("zhou", "engine"));
        final List<Long> userIds = new ArrayList<>(5);
        userIds.add(expectedUsers.get(4).getId());
        userIds.add(expectedUsers.get(0).getId());
        userIds.add(expectedUsers.get(2).getId());
        userIds.add(expectedUsers.get(1).getId());
        userIds.add(expectedUsers.get(3).getId());

        final Map<Long, User> usersResult = getIdentityAPI().getUsers(userIds);
        assertNotNull(usersResult);
        assertEquals(5, usersResult.size());
        assertEquals(expectedUsers.get(0), usersResult.get(expectedUsers.get(0).getId()));
        assertEquals(expectedUsers.get(1), usersResult.get(expectedUsers.get(1).getId()));
        assertEquals(expectedUsers.get(2), usersResult.get(expectedUsers.get(2).getId()));
        assertEquals(expectedUsers.get(3), usersResult.get(expectedUsers.get(3).getId()));
        assertEquals(expectedUsers.get(4), usersResult.get(expectedUsers.get(4).getId()));

        deleteUsers(expectedUsers);
    }

    @Test
    public void checkCreatedByForCreatedUser() throws BonitaException {
        final User userCreated = getIdentityAPI().createUser("bonitasoft", "123456");
        assertNotNull(userCreated);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        final User user = getIdentityAPI().getUserByUserName("bonitasoft");
        assertNotNull(user);
        assertNotNull(user.getCreatedBy());
        assertEquals(getSession().getUserId(), user.getCreatedBy());
        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Test
    public void getUserIds() throws BonitaException {
        final User matti = getIdentityAPI().createUser("matti", "bpm");
        final User jani = getIdentityAPI().createUser("jani", "bpm");

        final List<String> userNames = new ArrayList<>(3);
        userNames.add("jani");
        userNames.add("liisa");
        userNames.add("matti");

        final Map<String, User> userIds = getIdentityAPI().getUsersByUsernames(userNames);
        assertEquals(2, userIds.size());
        assertEquals(jani, userIds.get("jani"));
        assertEquals(matti, userIds.get("matti"));

        deleteUsers(matti, jani);
    }

    @Test
    public void getUserWithProContactData() throws BonitaException {
        final String james = "james";
        final User user = getIdentityAPI().createUser(james, "mbp");
        final UserUpdater updateDescriptor = buildUserUpdaterWithProContact();
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);
        final ContactData userContactData = getIdentityAPI().getUserContactData(updatedUser.getId(), false);

        final UserWithContactData proUser = getIdentityAPI().getUserWithProfessionalDetails(updatedUser.getId());
        assertEquals(userContactData, proUser.getContactData());
        assertEquals(updatedUser, proUser.getUser());

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    @Test
    public void getUserWithoutProContactData() throws BonitaException {
        final String james = "james";
        final User user = getIdentityAPI().createUser(james, "mbp");
        final UserUpdater updateDescriptor = buildUserUpdater();
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);

        final UserWithContactData proUser = getIdentityAPI().getUserWithProfessionalDetails(updatedUser.getId());
        assertNull(proUser.getContactData());
        assertEquals(updatedUser, proUser.getUser());

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    @Test(expected = UserNotFoundException.class)
    public void throwExceptionWhenGettingUnknownUserWithProContactData() throws BonitaException {
        getIdentityAPI().getUserWithProfessionalDetails(-45L);
    }

    @Test(expected = UserNotFoundException.class)
    public void throwExceptionWhenGettingTechnicalUserWithProContactData() throws BonitaException {
        getIdentityAPI().getUserWithProfessionalDetails(-1L);
    }

    @Test
    public void can_create_user_with_255_char_in_fields() throws Exception {
        final UserCreator creator = new UserCreator(completeWithZeros("user"), "bpm");
        creator.setJobTitle(completeWithZeros("Engineer"));
        creator.setFirstName(completeWithZeros("First"));
        creator.setLastName(completeWithZeros("Last"));

        final ContactDataCreator contactDataCreator = new ContactDataCreator();
        contactDataCreator.setAddress(completeWithZeros("32 Rue Gustave Eiffel"));
        creator.setProfessionalContactData(contactDataCreator);

        //when
        final User user = getIdentityAPI().createUser(creator);

        //then
        assertThat(user).isNotNull();
        assertThat(user.getUserName()).hasSize(255);
        assertThat(user.getFirstName()).hasSize(255);
        assertThat(user.getLastName()).hasSize(255);
        assertThat(user.getJobTitle()).hasSize(255);

        //when
        final UserWithContactData userWithContactData = getIdentityAPI().getUserWithProfessionalDetails(user.getId());

        //then
        assertThat(userWithContactData).isNotNull();
        assertThat(userWithContactData.getContactData().getAddress()).hasSize(255);

        //clean
        getIdentityAPI().deleteUser(user.getId());

    }

    private String completeWithZeros(final String prefix) {

        final StringBuilder stb = new StringBuilder(prefix);
        for (int i = 0; i < 255 - prefix.length(); i++) {
            stb.append("0");
        }

        return stb.toString();
    }

    @Test
    public void login_should_work_with_specific_characters() throws Exception {
        final User jyri = createUser("Jyri", "1234");

        final UserUpdater updater = new UserUpdater();
        updater.setPassword("ñ1234");
        getIdentityAPI().updateUser(jyri.getId(), updater);

        loginOnDefaultTenantWith("Jyri", "ñ1234");
        assertThat(getApiClient().getSession()).isNotNull();

        getIdentityAPI().deleteUser(jyri.getId());
    }

    @Test
    public void theTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        User john = createUser("john", "bpm");
        User jack = createUser("jack", "bpm");
        Date connection1 = getIdentityAPI().getUserByUserName("john").getLastConnection();
        Thread.sleep(20);
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        Date connection2 = getIdentityAPI().getUserByUserName("john").getLastConnection();
        getIdentityAPI().getUserByUserName("john").getLastConnection();
        Thread.sleep(20);
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        Date connection3 = getIdentityAPI().getUserByUserName("john").getLastConnection();
        Thread.sleep(20);
        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");
        Date connection4 = getIdentityAPI().getUserByUserName("john").getLastConnection();
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();

        deleteUsers(john, jack);

        assertThat(connection1).isNull();
        assertThat(connection2).isBefore(connection3);
        assertThat(connection3).isEqualTo(connection4);

    }

    @Test
    public void should_create_user_with_icon_create_the_icon() throws Exception {
        //given
        User user = getIdentityAPI().createUser(new UserCreator("userWithIcon", "thePassword").setIcon("myAvatar.jpg", "avatarContent".getBytes()));
        //when
        Icon icon = getIdentityAPI().getIcon(user.getIconId());
        //then
        assertThat(icon).isEqualTo(new IconImpl(icon.getId(), "image/jpeg", "avatarContent".getBytes()));
        //cleanup
        getIdentityAPI().deleteUser("userWithIcon");
    }

    @Test
    public void should_delete_user_delete_the_icon() throws Exception {
        //given
        User user = getIdentityAPI().createUser(new UserCreator("userWithIcon", "thePassword").setIcon("myAvatar.jpg", "avatarContent".getBytes()));
        //when
        getIdentityAPI().deleteUser(user.getId());
        //then
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("unable to find icon with id " + user.getIconId());
        try {
            getIdentityAPI().getIcon(user.getIconId());
        } finally {
            getIdentityAPI().deleteUser("userWithIcon");
        }

    }

    @Test
    public void should_update_user_create_a_new_icon() throws Exception {
        //given
        User user = getIdentityAPI().createUser(new UserCreator("userWithIcon", "thePassword"));
        //when
        User updatedUser = getIdentityAPI().updateUser(user.getId(), new UserUpdater().setIcon("myFile.png", "content".getBytes()));
        //then
        Icon icon = getIdentityAPI().getIcon(updatedUser.getIconId());
        assertThat(icon).isEqualTo(new IconImpl(icon.getId(), "image/png", "content".getBytes()));
        //cleanup
        getIdentityAPI().deleteUser("userWithIcon");
    }

    @Test
    public void should_update_user_with_new_icon_create_a_new_icon() throws Exception {
        //given
        User user = getIdentityAPI().createUser(new UserCreator("userWithIcon", "thePassword").setIcon("myAvatar.png", "avatarContent".getBytes()));
        //when
        User updatedUser = getIdentityAPI().updateUser(user.getId(), new UserUpdater().setIcon("myFile.jpg", "content".getBytes()));
        //then
        Icon newIcon = getIdentityAPI().getIcon(updatedUser.getIconId());
        assertThat(newIcon.getId()).isNotEqualTo(user.getIconId());
        assertThat(newIcon.getMimeType()).isEqualTo("image/jpeg");
        assertThat(newIcon.getContent()).isEqualTo("content".getBytes());
        //cleanup
        getIdentityAPI().deleteUser("userWithIcon");
    }

    @Test
    public void should_update_user_delete_the_icon() throws Exception {
        //given
        User user = getIdentityAPI().createUser(new UserCreator("userWithIcon", "thePassword").setIcon("myAvatar.jpg", "avatarContent".getBytes()));
        Icon icon = getIdentityAPI().getIcon(user.getIconId());
        //when
        User updateUser = getIdentityAPI().updateUser(user.getId(), new UserUpdater().setIcon(null, null));
        //then
        try {
            getIdentityAPI().getIcon(icon.getId());
            fail();
        } catch (NotFoundException ignored) {
        }
        assertThat(updateUser.getIconId()).isNull();
        //cleanup
        getIdentityAPI().deleteUser("userWithIcon");
    }

}
