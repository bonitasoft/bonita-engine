package org.bonitasoft.engine.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.NodeNotStartedException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserTest extends CommonAPITest {

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    /**
     * This test is here for arbitrary reason: it has to be tested on ANY API call.
     */
    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "Get exception when calling a platform method on node not started", jira = "ENGINE-1780")
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

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Create" }, jira = "ENGINE-577")
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

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Create" }, jira = "ENGINE-577")
    @Test
    public void createEnabledUserByAUser() throws BonitaException {
        final User user = getIdentityAPI().createUser("bonitasoft", "bpm");
        assertNotNull(user);
        assertTrue(user.isEnabled());

        getIdentityAPI().deleteUser("bonitasoft");
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Create" }, jira = "ENGINE-577")
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
        final Map<Long, User> users = getIdentityAPI().getUsers(Arrays.asList(-1l));
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

        final List<Long> userIds = new ArrayList<Long>();
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

        final List<Long> userIds = new ArrayList<Long>();
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

    @Cover(classes = { User.class, ContactData.class }, concept = BPMNConcept.ORGANIZATION, jira = "ENGINE-1055", keywords = { "contact info", "user" })
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
        final List<Long> userIds = new ArrayList<Long>();
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
        final List<Long> userIds = new ArrayList<Long>();
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
        final UserUpdater updateDescriptor = new UserUpdater();
        final String firstName = "Changed first name";
        updateDescriptor.setFirstName(firstName);
        final String iconName = "new icon name";
        updateDescriptor.setIconName(iconName);
        final String iconPath = "new_icon_path";
        updateDescriptor.setIconPath(iconPath);
        final String jobTitle = "New job title";
        updateDescriptor.setJobTitle(jobTitle);
        final String lastName = "Modified Last name";
        updateDescriptor.setLastName(lastName);
        final long managerId = 12354L;
        updateDescriptor.setManagerId(managerId);
        final String password = "Ch4n63D_P455W0RD";
        updateDescriptor.setPassword(password);
        final String username = "new_user_name";
        updateDescriptor.setUserName(username);

        final ContactDataUpdater persoDataUpDescr = new ContactDataUpdater();
        final String address = "3 rue des lilas";
        persoDataUpDescr.setAddress(address);
        final String building = "SkyScrapper";
        persoDataUpDescr.setBuilding(building);
        final String city = "Lyon";
        persoDataUpDescr.setCity(city);
        final String country = "Lichtenstein";
        persoDataUpDescr.setCountry(country);
        final String email = "noreply@yahoo.es";
        persoDataUpDescr.setEmail(email);
        final String faxNumber = "01-020013021452";
        persoDataUpDescr.setFaxNumber(faxNumber);
        final String mobileNumber = "06-02-000000";
        persoDataUpDescr.setMobileNumber(mobileNumber);
        final String phoneNumber = "04-76-000000";
        persoDataUpDescr.setPhoneNumber(phoneNumber);
        final String room = "Home";
        persoDataUpDescr.setRoom(room);
        final String state = "Rhône";
        persoDataUpDescr.setState(state);
        final String website = "http://perso.bonitasoft.com";
        persoDataUpDescr.setWebsite(website);
        final String zipCode = "69000";
        persoDataUpDescr.setZipCode(zipCode);

        final ContactDataUpdater proDataUpDescr = new ContactDataUpdater();
        final String address2 = "34 Gustave Eiffel";
        proDataUpDescr.setAddress(address2);
        final String building2 = "BigBlock";
        proDataUpDescr.setBuilding(building2);
        final String city2 = "L.A.";
        proDataUpDescr.setCity(city2);
        final String country2 = "Spain";
        proDataUpDescr.setCountry(country2);
        final String email2 = "noreply@bonitasoft.com";
        proDataUpDescr.setEmail(email2);
        final String faxNumber2 = "01-356743254";
        proDataUpDescr.setFaxNumber(faxNumber2);
        final String mobileNumber2 = "06-02-1111111";
        proDataUpDescr.setMobileNumber(mobileNumber2);
        final String phoneNumber2 = "04-76-111111";
        proDataUpDescr.setPhoneNumber(phoneNumber2);
        final String room2 = "A304";
        proDataUpDescr.setRoom(room2);
        final String state2 = "Isere";
        proDataUpDescr.setState(state2);
        final String website2 = "http://www.bonitasoft.com";
        proDataUpDescr.setWebsite(website2);
        final String zipCode2 = "38000";
        proDataUpDescr.setZipCode(zipCode2);
        final String title = "titre";
        updateDescriptor.setTitle(title);
        updateDescriptor.setPersonalContactData(persoDataUpDescr);
        updateDescriptor.setProfessionalContactData(proDataUpDescr);
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);
        assertNotNull(updatedUser);
        assertEquals(username, updatedUser.getUserName());
        assertEquals(firstName, updatedUser.getFirstName());
        assertEquals(iconName, updatedUser.getIconName());
        assertEquals(iconPath, updatedUser.getIconPath());
        assertEquals(jobTitle, updatedUser.getJobTitle());
        assertEquals(lastName, updatedUser.getLastName());
        assertEquals(managerId, updatedUser.getManagerUserId());
        assertNotSame(password, updatedUser.getPassword());

        final ContactData persoData = getIdentityAPI().getUserContactData(updatedUser.getId(), true);
        assertEquals(address, persoData.getAddress());
        assertEquals(building, persoData.getBuilding());
        assertEquals(city, persoData.getCity());
        assertEquals(country, persoData.getCountry());
        assertEquals(email, persoData.getEmail());
        assertEquals(faxNumber, persoData.getFaxNumber());
        assertEquals(mobileNumber, persoData.getMobileNumber());
        assertEquals(phoneNumber, persoData.getPhoneNumber());
        assertEquals(room, persoData.getRoom());
        assertEquals(state, persoData.getState());
        assertEquals(website, persoData.getWebsite());
        assertEquals(zipCode, persoData.getZipCode());
        final ContactData proData = getIdentityAPI().getUserContactData(updatedUser.getId(), false);
        assertEquals(address2, proData.getAddress());
        assertEquals(building2, proData.getBuilding());
        assertEquals(city2, proData.getCity());
        assertEquals(country2, proData.getCountry());
        assertEquals(email2, proData.getEmail());
        assertEquals(faxNumber2, proData.getFaxNumber());
        assertEquals(mobileNumber2, proData.getMobileNumber());
        assertEquals(phoneNumber2, proData.getPhoneNumber());
        assertEquals(room2, proData.getRoom());
        assertEquals(state2, proData.getState());
        assertEquals(website2, proData.getWebsite());
        assertEquals(zipCode2, proData.getZipCode());
        assertEquals(title, updatedUser.getTitle());

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Enabled", "User", "Update" }, jira = "ENGINE-577")
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

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Update" }, jira = "ENGINE-577")
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

    @Cover(classes = { IdentityAPI.class, ContactDataUpdater.class, User.class, ContactData.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "update",
            "user", "contact data" }, jira = "")
    @Test
    public void updateUserWithOnlyDataChanging() throws BonitaException {
        final User user = getIdentityAPI().createUser("james", "mbp");

        final ContactDataUpdater persoDataUpDescr = new ContactDataUpdater();
        final String address = "3 rue des lilas";
        persoDataUpDescr.setAddress(address);
        final String building = "SkyScrapper";
        persoDataUpDescr.setBuilding(building);
        final String city = "Lyon";
        persoDataUpDescr.setCity(city);
        final String country = "Lichtenstein";
        persoDataUpDescr.setCountry(country);
        final String email = "noreply@yahoo.es";
        persoDataUpDescr.setEmail(email);
        final String faxNumber = "01-020013021452";
        persoDataUpDescr.setFaxNumber(faxNumber);
        final String mobileNumber = "06-02-000000";
        persoDataUpDescr.setMobileNumber(mobileNumber);
        final String phoneNumber = "04-76-000000";
        persoDataUpDescr.setPhoneNumber(phoneNumber);
        final String room = "Home";
        persoDataUpDescr.setRoom(room);
        final String state = "Rhône";
        persoDataUpDescr.setState(state);
        final String website = "http://perso.bonitasoft.com";
        persoDataUpDescr.setWebsite(website);
        final String zipCode = "69000";
        persoDataUpDescr.setZipCode(zipCode);

        final ContactDataUpdater proDataUpDescr = new ContactDataUpdater();
        final String address2 = "34 Gustave Eiffel";
        proDataUpDescr.setAddress(address2);
        final String building2 = "BigBlock";
        proDataUpDescr.setBuilding(building2);
        final String city2 = "L.A.";
        proDataUpDescr.setCity(city2);
        final String country2 = "Spain";
        proDataUpDescr.setCountry(country2);
        final String email2 = "noreply@bonitasoft.com";
        proDataUpDescr.setEmail(email2);
        final String faxNumber2 = "01-356743254";
        proDataUpDescr.setFaxNumber(faxNumber2);
        final String mobileNumber2 = "06-02-1111111";
        proDataUpDescr.setMobileNumber(mobileNumber2);
        final String phoneNumber2 = "04-76-111111";
        proDataUpDescr.setPhoneNumber(phoneNumber2);
        final String room2 = "A304";
        proDataUpDescr.setRoom(room2);
        final String state2 = "Isere";
        proDataUpDescr.setState(state2);
        final String website2 = "http://www.bonitasoft.com";
        proDataUpDescr.setWebsite(website2);
        final String zipCode2 = "38000";
        proDataUpDescr.setZipCode(zipCode2);

        final UserUpdater updater = new UserUpdater();
        updater.setPersonalContactData(persoDataUpDescr);
        updater.setProfessionalContactData(proDataUpDescr);
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updater);
        assertNotNull(updatedUser);

        final ContactData persoData = getIdentityAPI().getUserContactData(updatedUser.getId(), true);
        assertEquals(address, persoData.getAddress());
        assertEquals(building, persoData.getBuilding());
        assertEquals(city, persoData.getCity());
        assertEquals(country, persoData.getCountry());
        assertEquals(email, persoData.getEmail());
        assertEquals(faxNumber, persoData.getFaxNumber());
        assertEquals(mobileNumber, persoData.getMobileNumber());
        assertEquals(phoneNumber, persoData.getPhoneNumber());
        assertEquals(room, persoData.getRoom());
        assertEquals(state, persoData.getState());
        assertEquals(website, persoData.getWebsite());
        assertEquals(zipCode, persoData.getZipCode());
        final ContactData proData = getIdentityAPI().getUserContactData(updatedUser.getId(), false);
        assertEquals(address2, proData.getAddress());
        assertEquals(building2, proData.getBuilding());
        assertEquals(city2, proData.getCity());
        assertEquals(country2, proData.getCountry());
        assertEquals(email2, proData.getEmail());
        assertEquals(faxNumber2, proData.getFaxNumber());
        assertEquals(mobileNumber2, proData.getMobileNumber());
        assertEquals(phoneNumber2, proData.getPhoneNumber());
        assertEquals(room2, proData.getRoom());
        assertEquals(state2, proData.getState());
        assertEquals(website2, proData.getWebsite());
        assertEquals(zipCode2, proData.getZipCode());

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

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Search", "Users", "Filter", "Order", "Pagination",
            "Column not unique" }, jira = "ENGINE-1557")
    @Test
    public void searchUser() throws BonitaException {
        final List<User> users = new ArrayList<User>();
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

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Search", "User", "Enabled", "Disabled" }, story = "Search enabled/disabled users", jira = "ENGINE-821")
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

    @Cover(classes = { SearchOptionsBuilder.class, IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "SearchUser", "Apostrophe" }, jira = "ENGINE-366")
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
        builder.filter(UserSearchDescriptor.GROUP_ID, String.valueOf(group.getId()));
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
        builder.filter(UserSearchDescriptor.ROLE_ID, String.valueOf(role.getId()));
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
        builder.filter(UserSearchDescriptor.GROUP_ID, String.valueOf(group2.getId()));
        builder.filter(UserSearchDescriptor.ROLE_ID, String.valueOf(role1.getId()));
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
        builder.filter(UserSearchDescriptor.MANAGER_USER_ID, manager.getId());
        final SearchResult<User> searchUsers = getIdentityAPI().searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(jack, users.get(0));

        deleteUsers(john, manager, jack);
    }

    @Cover(classes = { IdentityAPI.class, SearchOptionsBuilder.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Search", "User", "Manager",
            "Not in team" }, jira = "ENGINE-569")
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
        final List<User> expectedUsers = new ArrayList<User>();
        expectedUsers.add(getIdentityAPI().createUser("zhao", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("qian", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("sun", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("li", "engine"));
        expectedUsers.add(getIdentityAPI().createUser("zhou", "engine"));
        final List<Long> userIds = new ArrayList<Long>(5);
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

    @Cover(jira = "ENGINE-1818", classes = { User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "User identifiers" })
    @Test
    public void getUserIds() throws BonitaException {
        final User matti = getIdentityAPI().createUser("matti", "bpm");
        final User jani = getIdentityAPI().createUser("jani", "bpm");

        final List<String> userNames = new ArrayList<String>(3);
        userNames.add("jani");
        userNames.add("liisa");
        userNames.add("matti");

        final Map<String, User> userIds = getIdentityAPI().getUsersByUsernames(userNames);
        assertEquals(2, userIds.size());
        assertEquals(jani, userIds.get("jani"));
        assertEquals(matti, userIds.get("matti"));

        deleteUsers(matti, jani);
    }

    @Cover(jira = "ENGINE-1825", classes = { UserWithContactData.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "user", "contact data" })
    @Test
    public void getUserWithProContactData() throws BonitaException {
        final String james = "james";
        final User user = getIdentityAPI().createUser(james, "mbp");
        final UserUpdater updateDescriptor = new UserUpdater();
        final String firstName = "Changed first name";
        updateDescriptor.setFirstName(firstName);
        final String iconName = "new icon name";
        updateDescriptor.setIconName(iconName);
        final String iconPath = "new_icon_path";
        updateDescriptor.setIconPath(iconPath);
        final String jobTitle = "New job title";
        updateDescriptor.setJobTitle(jobTitle);
        final String lastName = "Modified Last name";
        updateDescriptor.setLastName(lastName);
        final long managerId = 12354L;
        updateDescriptor.setManagerId(managerId);
        final String password = "Ch4n63D_P455W0RD";
        updateDescriptor.setPassword(password);
        final String username = "new_user_name";
        updateDescriptor.setUserName(username);

        final ContactDataUpdater proDataUpDescr = new ContactDataUpdater();
        final String address2 = "34 Gustave Eiffel";
        proDataUpDescr.setAddress(address2);
        final String building2 = "BigBlock";
        proDataUpDescr.setBuilding(building2);
        final String city2 = "L.A.";
        proDataUpDescr.setCity(city2);
        final String country2 = "Spain";
        proDataUpDescr.setCountry(country2);
        final String email2 = "noreply@bonitasoft.com";
        proDataUpDescr.setEmail(email2);
        final String faxNumber2 = "01-356743254";
        proDataUpDescr.setFaxNumber(faxNumber2);
        final String mobileNumber2 = "06-02-1111111";
        proDataUpDescr.setMobileNumber(mobileNumber2);
        final String phoneNumber2 = "04-76-111111";
        proDataUpDescr.setPhoneNumber(phoneNumber2);
        final String room2 = "A304";
        proDataUpDescr.setRoom(room2);
        final String state2 = "Isere";
        proDataUpDescr.setState(state2);
        final String website2 = "http://www.bonitasoft.com";
        proDataUpDescr.setWebsite(website2);
        final String zipCode2 = "38000";
        proDataUpDescr.setZipCode(zipCode2);
        final String title = "titre";
        updateDescriptor.setTitle(title);
        updateDescriptor.setProfessionalContactData(proDataUpDescr);
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);
        final ContactData userContactData = getIdentityAPI().getUserContactData(updatedUser.getId(), false);

        final UserWithContactData proUser = getIdentityAPI().getUserWithProfessionalDetails(updatedUser.getId());
        assertEquals(userContactData, proUser.getContactData());
        assertEquals(updatedUser, proUser.getUser());

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    @Cover(jira = "ENGINE-1825", classes = { UserWithContactData.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "user", "contact data" })
    @Test
    public void getUserWithoutProContactData() throws BonitaException {
        final String james = "james";
        final User user = getIdentityAPI().createUser(james, "mbp");
        final UserUpdater updateDescriptor = new UserUpdater();
        final String firstName = "Changed first name";
        updateDescriptor.setFirstName(firstName);
        final String iconName = "new icon name";
        updateDescriptor.setIconName(iconName);
        final String iconPath = "new_icon_path";
        updateDescriptor.setIconPath(iconPath);
        final String jobTitle = "New job title";
        updateDescriptor.setJobTitle(jobTitle);
        final String lastName = "Modified Last name";
        updateDescriptor.setLastName(lastName);
        final long managerId = 12354L;
        updateDescriptor.setManagerId(managerId);
        final String password = "Ch4n63D_P455W0RD";
        updateDescriptor.setPassword(password);
        final String username = "new_user_name";
        updateDescriptor.setUserName(username);
        final User updatedUser = getIdentityAPI().updateUser(user.getId(), updateDescriptor);

        final UserWithContactData proUser = getIdentityAPI().getUserWithProfessionalDetails(updatedUser.getId());
        assertNull(proUser.getContactData());
        assertEquals(updatedUser, proUser.getUser());

        getIdentityAPI().deleteUser(updatedUser.getId());
    }

    @Cover(jira = "ENGINE-1825", classes = { UserWithContactData.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "user", "contact data" })
    @Test(expected = UserNotFoundException.class)
    public void throwExceptionWhenGettingUnknownUserWithProContactData() throws BonitaException {
        getIdentityAPI().getUserWithProfessionalDetails(-45l);
    }

    @Cover(jira = "ENGINE-1825", classes = { UserWithContactData.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "user", "contact data" })
    @Test(expected = UserNotFoundException.class)
    public void throwExceptionWhenGettingTechnicalUserWithProContactData() throws BonitaException {
        getIdentityAPI().getUserWithProfessionalDetails(-1l);
    }
}
