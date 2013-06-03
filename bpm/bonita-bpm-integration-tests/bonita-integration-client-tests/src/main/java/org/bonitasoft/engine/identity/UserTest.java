package org.bonitasoft.engine.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class UserTest extends CommonAPITest {

    @Test(expected = LoginException.class)
    public void loginFailsWithNullUsername() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login(null, null);
    }

    @Test(expected = LoginException.class)
    public void loginFailsWithEmptyUsername() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login("", null);
    }

    @Cover(classes = LoginAPI.class, concept = BPMNConcept.NONE, keywords = { "Login", "Password" }, story = "Try to login with null password", jira = "ENGINE-622")
    @Test(expected = LoginException.class)
    public void loginFailsWithNullPassword() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login("matti", null);
    }

    @Cover(classes = LoginAPI.class, concept = BPMNConcept.NONE, keywords = { "Login", "Password" }, story = "Try to login with wrong password")
    @Test(expected = LoginException.class)
    public void loginFailsWithWrongPassword() throws BonitaException {
        final String userName = "Truc";
        APITestUtil.createUserOnDefaultTenant(userName, "goodPassword");
        try {
            final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
            loginTenant.login(userName, "WrongPassword");
            fail("Should not be reached");
        } finally {
            final APISession session = APITestUtil.loginDefaultTenant();
            final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
            identityAPI.deleteUser(userName);
        }
    }

    @Cover(classes = LoginAPI.class, concept = BPMNConcept.NONE, keywords = { "Login", "Password" }, story = "Try to login with empty password", jira = "ENGINE-622")
    @Test(expected = LoginException.class)
    public void loginFailsWithEmptyPassword() throws BonitaException {
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        loginTenant.login("matti", "");
    }

    @Test
    public void userLoginDefaultTenant() throws BonitaException, InterruptedException {
        final String userName = "matti";
        final String password = "tervetuloa";
        APITestUtil.createUserOnDefaultTenant(userName, password);

        final Date now = new Date();
        Thread.sleep(300);
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession apiSession = loginAPI.login(userName, password);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
        final User user = identityAPI.getUserByUserName(userName);
        identityAPI.deleteUser(userName);

        assertEquals(userName, user.getUserName());
        assertNotSame(password, user.getPassword());
        assertTrue(now.before(user.getLastConnection()));
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyPassword() throws BonitaException, InterruptedException {
        final String userName = "matti";
        final String password = "";
        APITestUtil.createUserOnDefaultTenant(userName, password);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyUserName() throws BonitaException, InterruptedException {
        final String userName = "";
        final String password = "revontuli";
        APITestUtil.createUserOnDefaultTenant(userName, password);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullPassword() throws BonitaException, InterruptedException {
        final String userName = "matti";
        APITestUtil.createUserOnDefaultTenant(userName, null);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullUserName() throws BonitaException, InterruptedException {
        final String password = "revontuli";
        APITestUtil.createUserOnDefaultTenant(null, password);
    }

    @Test
    public void createUserByUsernameAndPassword() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        identityAPI.getNumberOfUsers();
        final User userCreated = identityAPI.createUser("bonitasoft", "123456");
        assertNotNull(userCreated);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        final User user = identityAPI.getUserByUserName("bonitasoft");
        assertNotNull(user);
        identityAPI.deleteUser("bonitasoft");
        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Create" }, jira = "ENGINE-577")
    @Test
    public void createDisabledUserByUsernameAndPassword() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User userCreated = identityAPI.createUser("bonitasoft", "123456");
        assertNotNull(userCreated);

        final User user = identityAPI.getUserByUserName("bonitasoft");
        assertNotNull(user);
        assertEquals(false, user.isEnabled());

        identityAPI.deleteUser("bonitasoft");
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = AlreadyExistsException.class)
    public void createUserByUsernameAndPasswordException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User userCreated = identityAPI.createUser("bonitasoft", "123456");
        assertNotNull(userCreated);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        final User user = identityAPI.getUserByUserName("bonitasoft");
        assertNotNull(user);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        try {
            identityAPI.createUser("bonitasoft", "123456");
        } finally {
            identityAPI.deleteUser("bonitasoft");
            APITestUtil.logoutTenant(session);
        }
    }

    public void getFirstPageWithNoResult() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        identityAPI.getUsers(0, 10, UserCriterion.USER_NAME_ASC);
    }

    @Test(expected = CreationException.class)
    public void createUserFailed() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        identityAPI.createUser(null, null);
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = CreationException.class)
    public void createUserUsingNullUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        identityAPI.createUser(null);
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void createUserByAUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final String userName = "spring";
        final UserCreator creator = new UserCreator(userName, "bpm");
        creator.setTitle("wwwwwwwwwwwwwwwwwwwwwwwwwwww");
        final User user = identityAPI.createUser(creator);
        assertNotNull(user);
        assertEquals(userName, user.getUserName());

        identityAPI.deleteUser(userName);
        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Create" }, jira = "ENGINE-577")
    @Test
    public void createDisabledUserByAUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User user = identityAPI.createUser("bonitasoft", "bpm");
        assertNotNull(user);
        assertEquals(false, user.isEnabled());

        identityAPI.deleteUser("bonitasoft");
        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Disabled", "User", "Create" }, jira = "ENGINE-577")
    @Test
    public void createEnabledUserByAUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final UserCreator creator = new UserCreator("bonitasoft", "bpm");
        creator.setEnabled(true);
        final User user = identityAPI.createUser(creator);
        assertNotNull(user);
        assertEquals(true, user.isEnabled());

        identityAPI.deleteUser("bonitasoft");
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullUserNameUsingBuilder() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.createUser(null, "revontuli");
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithANullPasswordUsingBuilder() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.createUser("matti", null);
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyUserNameUsingBuilder() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.createUser("", "revontuli");
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test(expected = CreationException.class)
    public void cannotCreateAUserWithAnEmptyPasswordUsingBuilder() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.createUser("matti", "");
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test(expected = UserNotFoundException.class)
    public void getUserByUsernameWithException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.getUserByUserName("Bonita");
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test
    public void getUserByUsername() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        identityAPI.createUser("bonita", "password");
        final User user = identityAPI.getUserByUserName("bonita");
        assertEquals("bonita", user.getUserName());
        assertNotSame("password", user.getPassword());

        identityAPI.deleteUser("bonita");
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void getNumberOfUsers() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        identityAPI.createUser("jane", "bpm");
        identityAPI.createUser("paul", "bpm");

        final long usersCount = identityAPI.getNumberOfUsers();
        assertEquals(2, usersCount);

        identityAPI.deleteUser("jane");
        identityAPI.deleteUser("paul");
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void loginWithExistingUserAndCheckId() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final String userName = "corvinus";
        final String password = "underworld";
        final User user = identityAPI.createUser(userName, password);
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        final APISession login = loginTenant.login(userName, password);
        assertTrue("userId should be valuated", user.getId() != -1);
        assertEquals(user.getId(), login.getUserId());

        identityAPI.deleteUser(user.getId());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void getUserById() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User userCreated = identityAPI.createUser("zhang", "engine");
        final User user = identityAPI.getUser(userCreated.getId());
        assertNotNull(user);
        assertEquals("zhang", user.getUserName());
        assertNotSame("engine", user.getPassword());

        identityAPI.deleteUser("zhang");
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = UserNotFoundException.class)
    public void cannotGetTechUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.getUser(-1);
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test
    public void cannotGetTechUserInList() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final Map<Long, User> users = identityAPI.getUsers(Arrays.asList(-1l));
        assertNull(users.get(-1));
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = UserNotFoundException.class)
    public void getUserByIDWithUserNotFoundException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User userCreated = identityAPI.createUser("zhang", "engine");
        try {
            identityAPI.getUser(userCreated.getId() + 100);
        } finally {
            identityAPI.deleteUser("zhang");
            APITestUtil.logoutTenant(session);
        }
    }

    @Test
    public void getUsersByIDs() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User userCreated1 = identityAPI.createUser("zhang", "engine");
        final User userCreated2 = identityAPI.createUser("jmege", "engine");

        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(userCreated1.getId());
        userIds.add(userCreated2.getId());

        final Map<Long, User> users = identityAPI.getUsers(userIds);
        assertNotNull(users);
        assertEquals(2, users.size());

        assertEquals("zhang", users.get(userCreated1.getId()).getUserName());
        assertNotSame("engine", users.get(userCreated1.getId()).getPassword());

        assertEquals("jmege", users.get(userCreated2.getId()).getUserName());
        assertNotSame("engine", users.get(userCreated2.getId()).getPassword());

        identityAPI.deleteUser("zhang");
        identityAPI.deleteUser("jmege");
        APITestUtil.logoutTenant(session);
    }

    public void getUsersByIDsWithoutUserNotFoundException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User userCreated1 = identityAPI.createUser("zhang", "engine");
        final User userCreated2 = identityAPI.createUser("jmege", "engine");

        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(userCreated1.getId());
        userIds.add(userCreated2.getId() + 100);

        final Map<Long, User> users = identityAPI.getUsers(userIds);
        assertNotNull(users);
        assertEquals(1, users.size());

        assertEquals("zhang", users.get(userCreated1.getId()).getUserName());
        assertEquals("engine", users.get(userCreated1.getId()).getPassword());

        identityAPI.deleteUser("zhang");
        identityAPI.deleteUser("jmege");
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void deleteUserByUserName() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        identityAPI.createUser("testDelete", "engine");
        identityAPI.deleteUser("testDelete");
        assertEquals(0, identityAPI.getNumberOfUsers());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void deleteNonExistingUserByUserName() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final String userName = "testDelete";
        identityAPI.createUser(userName, "engine");
        identityAPI.deleteUser(userName);
        try {
            identityAPI.deleteUser(userName);
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test(expected = DeletionException.class)
    public void deleteUserByUserNameWithUserDeletionException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        identityAPI.deleteUser(null);
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void deleteUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User user = identityAPI.createUser("testDelete", "engine");
        identityAPI.deleteUser(user.getId());
        assertEquals(0, identityAPI.getNumberOfUsers());
        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { User.class, ContactData.class }, concept = BPMNConcept.ORGANIZATION, jira = "ENGINE-1055", keywords = { "contact info", "user" })
    @Test
    public void deleteUserContactInfo() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final UserCreator creator = new UserCreator("john", "bpm");
        creator.setFirstName("John").setLastName("Doe");
        final ContactDataCreator persoCreator = new ContactDataCreator();
        persoCreator.setEmail("john.doe@anywhere.com");
        final ContactDataCreator proCreator = new ContactDataCreator();
        proCreator.setEmail("john.doe@bonitasoft.com");
        creator.setPersonalContactData(persoCreator);
        creator.setProfessionalContactData(proCreator);
        final User john = identityAPI.createUser(creator);

        final ContactData persoData = identityAPI.getUserContactData(john.getId(), true);
        assertEquals("john.doe@anywhere.com", persoData.getEmail());
        final ContactData prooData = identityAPI.getUserContactData(john.getId(), false);
        assertEquals("john.doe@bonitasoft.com", prooData.getEmail());

        identityAPI.deleteUser(john.getId());
        assertEquals(0, identityAPI.getNumberOfUsers());
        assertNull(identityAPI.getUserContactData(john.getId(), true));

        APITestUtil.logoutTenant(session);
    }

    @Test
    public void deleteNonExistingUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User user = identityAPI.createUser("testDelete", "engine");
        identityAPI.deleteUser(user.getId());
        try {
            identityAPI.deleteUser(user.getId());
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test
    public void deleteUsers() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(identityAPI.createUser("user1", "engine").getId());
        userIds.add(identityAPI.createUser("user2", "engine").getId());
        userIds.add(identityAPI.createUser("user3", "engine").getId());
        userIds.add(identityAPI.createUser("user4", "engine").getId());
        userIds.add(identityAPI.createUser("user5", "engine").getId());

        identityAPI.deleteUsers(userIds);
        assertEquals(0, identityAPI.getNumberOfUsers());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void deleteNonExistingUsers() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        try {
            identityAPI.deleteUsers(null);
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test
    public void deleteUsersDeleteAllExistingOnesAndIgnoresOthers() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(identityAPI.createUser("user1", "engine").getId());
        userIds.add(identityAPI.createUser("user2", "engine").getId());
        userIds.add(152458L);

        identityAPI.deleteUsers(userIds);
        assertEquals(0, identityAPI.getNumberOfUsers());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void updateUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final String james = "james";
        final User user = identityAPI.createUser(james, "mbp");
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
        final User updatedUser = identityAPI.updateUser(user.getId(), updateDescriptor);
        assertNotNull(updatedUser);
        assertEquals(username, updatedUser.getUserName());
        assertEquals(firstName, updatedUser.getFirstName());
        assertEquals(iconName, updatedUser.getIconName());
        assertEquals(iconPath, updatedUser.getIconPath());
        assertEquals(jobTitle, updatedUser.getJobTitle());
        assertEquals(lastName, updatedUser.getLastName());
        assertEquals(managerId, updatedUser.getManagerUserId());
        assertNotSame(password, updatedUser.getPassword());

        final ContactData persoData = identityAPI.getUserContactData(updatedUser.getId(), true);
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
        final ContactData proData = identityAPI.getUserContactData(updatedUser.getId(), false);
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

        identityAPI.deleteUser(updatedUser.getId());
        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Enabled", "User", "Update" }, jira = "ENGINE-577")
    @Test
    public void updateUserToBeEnabled() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        // Create user, and updateDescriptor
        final User user = identityAPI.createUser("bonitasoft", "123456");
        assertNotNull(user);
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(true);

        // Update user
        final User updatedUser = identityAPI.updateUser(user.getId(), updateDescriptor);
        assertNotNull(updatedUser);
        assertEquals(true, updatedUser.isEnabled());

        // Clean
        identityAPI.deleteUser("bonitasoft");
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void updateUserManager() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User matti = identityAPI.createUser("matti", "bpm");
        final User james = identityAPI.createUser("james", "bpm");
        assertEquals(0, james.getManagerUserId());
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setManagerId(matti.getId());
        final User updatedUser = identityAPI.updateUser(james.getId(), updateDescriptor);
        assertNotNull(updatedUser);
        assertEquals(matti.getId(), updatedUser.getManagerUserId());

        identityAPI.deleteUser(james.getId());
        identityAPI.deleteUser(matti.getId());
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = UserNotFoundException.class)
    public void updateUserWithUserNotFoundException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setUserName("john");
        updateDescriptor.setPassword("bpm");
        final int userId = 100;
        try {
            identityAPI.updateUser(userId, updateDescriptor);
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test(expected = UpdateException.class)
    public void updateUserWithUserUpdateException() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User oldUser = identityAPI.createUser("old", "oldPassword");
        try {
            identityAPI.updateUser(oldUser.getId(), null);
        } finally {
            identityAPI.deleteUser(oldUser.getId());
            APITestUtil.logoutTenant(session);
        }
    }

    @Cover(classes = { IdentityAPI.class, ContactDataUpdater.class, User.class, ContactData.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "update",
            "user", "contact data" })
    @Test
    public void updateUserWithOnlyDataChanging() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User user = identityAPI.createUser("james", "mbp");

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
        final User updatedUser = identityAPI.updateUser(user.getId(), updater);
        assertNotNull(updatedUser);

        final ContactData persoData = identityAPI.getUserContactData(updatedUser.getId(), true);
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
        final ContactData proData = identityAPI.getUserContactData(updatedUser.getId(), false);
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

        identityAPI.deleteUser(updatedUser.getId());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void getPaginatedUsersWithUserCriterion() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        identityAPI.createUser("auser1o", "bpm", "a", "a");
        identityAPI.createUser("cuser2o", "bpm", "c", "c");
        identityAPI.createUser("buser3o", "bpm", "b", "b");
        identityAPI.createUser("euser4o", "bpm", "e", "e");
        identityAPI.createUser("duser5o", "bpm", "d", "d");

        List<User> usersASC = identityAPI.getUsers(0, 3, UserCriterion.USER_NAME_ASC);
        assertEquals(3, usersASC.size());
        assertEquals("auser1o", usersASC.get(0).getUserName());
        assertEquals("buser3o", usersASC.get(1).getUserName());
        assertEquals("cuser2o", usersASC.get(2).getUserName());
        usersASC = identityAPI.getUsers(3, 3, UserCriterion.USER_NAME_ASC);
        assertEquals(2, usersASC.size());
        assertEquals("duser5o", usersASC.get(0).getUserName());
        assertEquals("euser4o", usersASC.get(1).getUserName());
        final List<User> users = identityAPI.getUsers(6, 3, UserCriterion.USER_NAME_ASC);
        assertTrue(users.isEmpty());
        final List<User> usersDESC = identityAPI.getUsers(0, 3, UserCriterion.USER_NAME_DESC);
        assertEquals(3, usersDESC.size());
        assertEquals("euser4o", usersDESC.get(0).getUserName());
        assertEquals("duser5o", usersDESC.get(1).getUserName());
        assertEquals("cuser2o", usersDESC.get(2).getUserName());

        final List<User> usersFirstNameASC = identityAPI.getUsers(0, 3, UserCriterion.FIRST_NAME_ASC);
        assertEquals(3, usersFirstNameASC.size());
        assertEquals("a", usersFirstNameASC.get(0).getFirstName());
        assertEquals("b", usersFirstNameASC.get(1).getFirstName());
        assertEquals("c", usersFirstNameASC.get(2).getFirstName());

        final List<User> usersFirstNameDESC = identityAPI.getUsers(0, 3, UserCriterion.FIRST_NAME_DESC);
        assertEquals(3, usersFirstNameDESC.size());
        assertEquals("e", usersFirstNameDESC.get(0).getFirstName());
        assertEquals("d", usersFirstNameDESC.get(1).getFirstName());
        assertEquals("c", usersFirstNameDESC.get(2).getFirstName());

        final List<User> usersLastNameASC = identityAPI.getUsers(0, 3, UserCriterion.LAST_NAME_ASC);
        assertEquals(3, usersLastNameASC.size());
        assertEquals("a", usersLastNameASC.get(0).getLastName());
        assertEquals("b", usersLastNameASC.get(1).getLastName());
        assertEquals("c", usersLastNameASC.get(2).getLastName());

        final List<User> usersLastNameDESC = identityAPI.getUsers(0, 3, UserCriterion.LAST_NAME_DESC);
        assertEquals(3, usersLastNameDESC.size());
        assertEquals("e", usersLastNameDESC.get(0).getLastName());
        assertEquals("d", usersLastNameDESC.get(1).getLastName());
        assertEquals("c", usersLastNameDESC.get(2).getLastName());

        identityAPI.deleteUser("auser1o");
        identityAPI.deleteUser("buser3o");
        identityAPI.deleteUser("cuser2o");
        identityAPI.deleteUser("duser5o");
        identityAPI.deleteUser("euser4o");
        APITestUtil.logoutTenant(session);
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotCreateTwoUserWithTheSameUserName() throws BonitaException {
        final String username = "install";
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user1 = identityAPI.createUser(username, "bpm");
        try {
            identityAPI.createUser(username, "bos");
        } finally {
            identityAPI.deleteUser(user1.getId());
        }
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void loginWithTechnicalUser() throws BonitaException {
        final String username = "install";
        final String pwd = "install";
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        final APISession session = loginTenant.login(username, pwd);
        assertTrue("Should be logged in as Technical user", session.isTechnicalUser());
        loginTenant.logout(session);
    }

    @Test
    public void loginWithNonTechnicalUser() throws BonitaException {
        final String username = "install";
        final String pwd = "install";
        final LoginAPI loginTenant = TenantAPIAccessor.getLoginAPI();
        APISession session = loginTenant.login(username, pwd);
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser("matti", "kieli");
        loginTenant.logout(session);

        session = loginTenant.login("matti", "kieli");
        assertTrue("Should be logged in as a NON-Technical user", !session.isTechnicalUser());
        loginTenant.logout(session);

        session = loginTenant.login(username, pwd);
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        identityAPI.deleteUser(user.getId());
        loginTenant.logout(session);
    }

    @Test(expected = SearchException.class)
    public void searchUserWithWrongSortKey() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.FIRST_NAME, "Jean-Gustave");
        builder.sort("WRONG_SORT_KEY", Order.ASC);
        try {
            identityAPI.searchUsers(builder.done());
        } finally {
            APITestUtil.logoutTenant(session);
        }
    }

    @Test
    public void searchUser() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User john2 = identityAPI.createUser("john002", "bpm", "John", "Taylor");
        final User jack = identityAPI.createUser("jack001", "bpm", "Jack", "Jack");
        final User john1 = identityAPI.createUser("john001", "bpm", "John", "Smith");

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.FIRST_NAME, "John");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        List<User> users = searchUsers.getResult();
        assertEquals(john1, users.get(0));
        assertEquals(john2, users.get(1));

        // pagination test
        builder = new SearchOptionsBuilder(1, 1);
        builder.filter(UserSearchDescriptor.FIRST_NAME, "John");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        users = searchUsers.getResult();
        assertEquals(john2, users.get(0));

        identityAPI.deleteUser(john1.getId());
        identityAPI.deleteUser(john2.getId());
        identityAPI.deleteUser(jack.getId());

        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Search", "User", "Enabled", "Disabled" }, story = "Search enabled/disabled users", jira = "ENGINE-821")
    @Test
    public void searchEnabledDisabledUsers() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        // Create users
        final User john = identityAPI.createUser("john002", "bpm", "John", "Taylor");
        final User jack = identityAPI.createUser("jack001", "bpm", "Jack", "Doe");

        // Disabled jack
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(true);
        final User updatedJack = identityAPI.updateUser(jack.getId(), updateDescriptor);

        // Search enabled users
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.ENABLED, true);
        SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        List<User> users = searchUsers.getResult();
        assertEquals(updatedJack, users.get(0));

        // Search disabled users
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.ENABLED, false);
        searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        users = searchUsers.getResult();
        assertEquals(john, users.get(0));

        // Clean up
        identityAPI.deleteUser(john.getId());
        identityAPI.deleteUser(jack.getId());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void searchUserUsingTerm() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User john2 = identityAPI.createUser("john002", "bpm", "John", "Taylor");
        final User jack = identityAPI.createUser("jack001", "bpm", "Jack", "Doe");
        final User john1 = identityAPI.createUser("john001", "bpm", "John", "Smith");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("Jo");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.ASC);
        final SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(2, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(john1, users.get(0));
        assertEquals(john2, users.get(1));

        identityAPI.deleteUser(john1.getId());
        identityAPI.deleteUser(john2.getId());
        identityAPI.deleteUser(jack.getId());

        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { SearchOptionsBuilder.class, IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "SearchUser", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchUserWithApostrophe() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User user1 = identityAPI.createUser("'john'002", "bpm", "John", "A");
        final User user3 = identityAPI.createUser("c", "bpm", "'john'n", "C");
        final User user4 = identityAPI.createUser("d", "bpm", "'d", "John'");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("'");
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);
        final SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(3, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(user4, users.get(0));
        assertEquals(user3, users.get(1));
        assertEquals(user1, users.get(2));

        identityAPI.deleteUser(user1.getId());
        identityAPI.deleteUser(user3.getId());
        identityAPI.deleteUser(user4.getId());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void searchTermWithSpecialChars() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("Séba");
        final SearchOptions searchOptions = builder.done();
        SearchResult<User> searchUsers = identityAPI.searchUsers(searchOptions);
        assertEquals(0, searchUsers.getCount());

        final User friend = identityAPI.createUser("Sébastien", "ENCRYPTED");

        searchUsers = identityAPI.searchUsers(searchOptions);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(friend, users.get(0));

        identityAPI.deleteUser(friend.getId());

        APITestUtil.logoutTenant(session);
    }

    @Test
    public void searchTermIsInsensitive() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User manu = identityAPI.createUser("emmanuel", "ENCRYPTED", "manuel", "Duch");
        final User marie = identityAPI.createUser("Marie", "bpm", "Marie", "Gillain");
        final User marcel = identityAPI.createUser("marcel", "bpm", "marcel", "Desaillie");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("Ma");
        builder.sort(UserSearchDescriptor.FIRST_NAME, Order.ASC);
        final SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(3, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(manu, users.get(0));
        assertEquals(marcel, users.get(1));
        assertEquals(marie, users.get(2));

        identityAPI.deleteUser(marcel.getId());
        identityAPI.deleteUser(manu.getId());
        identityAPI.deleteUser(marie.getId());

        APITestUtil.logoutTenant(session);
    }

    @Test
    public void testSearchUsersInGroup() throws BonitaException {
        login();

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
        deleteUser(john1.getId());
        deleteUser(john2.getId());
        deleteUser(jack.getId());

        logout();
    }

    @Test
    public void testSearchUsersInRole() throws BonitaException {
        login();

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
        deleteUser(john1.getId());
        deleteUser(john2.getId());
        deleteUser(jack.getId());

        logout();
    }

    @Test
    public void testSearchUsersInRoleAndGroup() throws BonitaException {
        login();

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

        getIdentityAPI().deleteGroup(group1.getId());
        getIdentityAPI().deleteRole(role1.getId());
        getIdentityAPI().deleteGroup(group2.getId());
        getIdentityAPI().deleteRole(role2.getId());
        deleteUser(john1.getId());
        deleteUser(john2.getId());
        deleteUser(jack.getId());

        logout();
    }

    @Test
    public void searchTeamMembers() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final User manager = identityAPI.createUser("john002", "bpm", "John", "Taylor");
        final UserCreator creator = new UserCreator("jack001", "bpm");
        creator.setFirstName("Jack").setLastName("Doe").setManagerUserId(manager.getId());
        final User jack = identityAPI.createUser(creator);
        final User john = identityAPI.createUser("john001", "bpm", "John", "Smith");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(UserSearchDescriptor.MANAGER_USER_ID, manager.getId());
        final SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(jack, users.get(0));

        identityAPI.deleteUser(john.getId());
        identityAPI.deleteUser(manager.getId());
        identityAPI.deleteUser(jack.getId());

        APITestUtil.logoutTenant(session);
    }

    @Cover(classes = { IdentityAPI.class, SearchOptionsBuilder.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Search", "User", "Manager",
            "Not in team" }, jira = "ENGINE-569")
    @Test
    public void searchUsersNotInTeam() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        // Manager
        final User manager = identityAPI.createUser("john002", "bpm", "John", "Taylor");

        // User with manager
        final UserCreator creator = new UserCreator("jack001", "bpm");
        creator.setFirstName("Jack").setLastName("Doe").setManagerUserId(manager.getId());
        final User jack = identityAPI.createUser(creator);

        // User without manager
        final User john = identityAPI.createUser("john003", "bpm", "John", "Smith");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.differentFrom(UserSearchDescriptor.MANAGER_USER_ID, manager.getId()).differentFrom(UserSearchDescriptor.USER_NAME, manager.getUserName());
        final SearchResult<User> searchUsers = identityAPI.searchUsers(builder.done());
        assertNotNull(searchUsers);
        assertEquals(1, searchUsers.getCount());
        final List<User> users = searchUsers.getResult();
        assertEquals(john, users.get(0));

        identityAPI.deleteUser(john.getId());
        identityAPI.deleteUser(manager.getId());
        identityAPI.deleteUser(jack.getId());
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void getUsersFromIds() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User userCreated1 = identityAPI.createUser("zhao", "engine");
        final User userCreated2 = identityAPI.createUser("qian", "engine");
        final User userCreated3 = identityAPI.createUser("sun", "engine");
        final User userCreated4 = identityAPI.createUser("li", "engine");
        final User userCreated5 = identityAPI.createUser("zhou", "engine");
        final long id1 = userCreated1.getId();
        final long id2 = userCreated2.getId();
        final long id3 = userCreated3.getId();
        final long id4 = userCreated4.getId();
        final long id5 = userCreated5.getId();
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(id5);
        userIds.add(id1);
        userIds.add(id3);
        userIds.add(id2);
        userIds.add(id4);

        final Map<Long, User> users = identityAPI.getUsers(userIds);
        assertNotNull(users);
        assertEquals(5, users.size());
        assertEquals(userCreated1, users.get(id1));
        assertEquals(userCreated2, users.get(id2));
        assertEquals(userCreated3, users.get(id3));
        assertEquals(userCreated4, users.get(id4));
        assertEquals(userCreated5, users.get(id5));

        for (final Long userId : userIds) {
            identityAPI.deleteUser(userId);

        }
        APITestUtil.logoutTenant(session);
    }

    @Test
    public void checkCreatedByForCreatedUser() throws BonitaException, BonitaHomeNotSetException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User userCreated = identityAPI.createUser("bonitasoft", "123456");
        assertNotNull(userCreated);
        assertEquals("bonitasoft", userCreated.getUserName());
        assertNotSame("123456", userCreated.getPassword());
        final User user = identityAPI.getUserByUserName("bonitasoft");
        assertNotNull(user);
        assertNotNull(user.getCreatedBy());
        assertEquals(session.getUserId(), user.getCreatedBy());
        identityAPI.deleteUser("bonitasoft");
        APITestUtil.logoutTenant(session);
    }

}
