/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.identity.GroupCreator.GroupField;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.StartProcessUntilStep;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class OrganizationIT extends TestWithTechnicalUser {

    private static final String SKILLS_VALUE = "Java";

    private static final String SKILLS_UPDATED_VALUE = "Java, Groovy";

    private static final String LOCATION_VALUE = "Engineering";

    private static final String QA = "QA";

    private static final String ENGINE = "Engine";

    private static final String WEB_TEAM_MANAGER = "Web Team Manager";

    private static final String JOHNNYFOOTBALL = "johnnyfootball";

    private static final String DEVELOPER = "Developer";

    private static final String WEB_TEAM = "web team";

    private static final String BONITA_MANAGER = "Bonita Manager";

    private static final String MANAGER = "Manager";

    private static final String WEB_GROUP_NAME = "Web";

    private static final String LIUYANYAN_USERNAME = "liuyanyan";

    private static final String ANTHONY_USERNAME = "anthony.birembault";

    private static final String SKILLS_DESCRIPTION = "The user skills";

    private static final String SKILLS_NAME = "Skills";

    private static final String LOCATION_NAME = "Office location";

    private static final String UTF_8 = "UTF-8";

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule();

    @Test
    public void importOrganization() throws Exception {
        // when
        importOrganization("simpleOrganization.xml");

        // then
        final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap = checkDefaultCustomUserInfoDefinitons();
        checkDefaultUsers();
        checkDefaultCustomUserInfoValues(userInfoDefinitonsMap);
        checkDefaultGroups();
        checkDefaultRoles();
        checkDefaultMembership();

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    private void checkDefaultCustomUserInfoValues(final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap)
            throws Exception {
        checkDefaultCustomUserInfoValueForFirstUser(userInfoDefinitonsMap);
        checkDefaultCustomUserInfoValueForSecondUser(userInfoDefinitonsMap);
    }

    private void checkCustomUserInfoValuesAfterUpdate(final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap)
            throws Exception {
        // the first user is not present in the second file to import, so his information keep the same
        checkDefaultCustomUserInfoValueForFirstUser(userInfoDefinitonsMap);
        checkCustomUserInfoValueForSecondUserAfterUpdate(userInfoDefinitonsMap);
    }

    private void checkDefaultCustomUserInfoValueForSecondUser(
            final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap) throws UserNotFoundException {
        final User user = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        final SearchOptions searchOptions = getCustomUserInfoValueSearchOptions(user);
        final SearchResult<CustomUserInfoValue> searchResult = getIdentityAPI()
                .searchCustomUserInfoValues(searchOptions);
        assertThat(searchResult.getCount()).isEqualTo(1);
        checkCustomUserInfo(searchResult.getResult().get(0), SKILLS_NAME, SKILLS_VALUE, userInfoDefinitonsMap);
    }

    private void checkCustomUserInfoValueForSecondUserAfterUpdate(
            final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap)
            throws UserNotFoundException {
        final User user = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        final SearchOptions searchOptions = getCustomUserInfoValueSearchOptions(user);
        final SearchResult<CustomUserInfoValue> searchResult = getIdentityAPI()
                .searchCustomUserInfoValues(searchOptions);
        assertThat(searchResult.getCount()).isEqualTo(2);
        checkCustomUserInfo(searchResult.getResult().get(0), LOCATION_NAME, LOCATION_VALUE, userInfoDefinitonsMap);
        checkCustomUserInfo(searchResult.getResult().get(1), SKILLS_NAME, SKILLS_UPDATED_VALUE, userInfoDefinitonsMap);
    }

    private void checkDefaultCustomUserInfoValueForFirstUser(
            final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap) throws UserNotFoundException {
        final User user = getIdentityAPI().getUserByUserName(ANTHONY_USERNAME);
        final SearchOptions searchOptions = getCustomUserInfoValueSearchOptions(user);

        final SearchResult<CustomUserInfoValue> searchResult = getIdentityAPI()
                .searchCustomUserInfoValues(searchOptions);
        assertThat(searchResult.getCount()).isEqualTo(2);

        checkCustomUserInfo(searchResult.getResult().get(0), LOCATION_NAME, LOCATION_VALUE, userInfoDefinitonsMap);
        checkCustomUserInfo(searchResult.getResult().get(1), SKILLS_NAME, SKILLS_VALUE, userInfoDefinitonsMap);
    }

    private SearchOptions getCustomUserInfoValueSearchOptions(final User user) {
        final SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, 10);
        optionsBuilder.filter(CustomUserInfoValueSearchDescriptor.USER_ID, user.getId());
        optionsBuilder.sort(CustomUserInfoValueSearchDescriptor.DEFINITION_ID, Order.ASC);
        return optionsBuilder.done();
    }

    private void checkCustomUserInfo(final CustomUserInfoValue customUserInfoValue, final String expectedName,
            final String expectedValue,
            final Map<String, CustomUserInfoDefinition> userInfoDefinitonsMap) {
        assertThat(customUserInfoValue.getDefinitionId()).isEqualTo(userInfoDefinitonsMap.get(expectedName).getId());
        assertThat(customUserInfoValue.getValue()).isEqualTo(expectedValue);
    }

    private Map<String, CustomUserInfoDefinition> checkDefaultCustomUserInfoDefinitons() {
        final List<CustomUserInfoDefinition> customUserInfoDefinitions = getIdentityAPI()
                .getCustomUserInfoDefinitions(0, 10);
        assertThat(customUserInfoDefinitions.size()).isEqualTo(2);

        final CustomUserInfoDefinition firstDefinition = customUserInfoDefinitions.get(0);
        final CustomUserInfoDefinition secondDefinition = customUserInfoDefinitions.get(1);

        checkCustomUserInfoDefinition(LOCATION_NAME, null, firstDefinition);
        checkCustomUserInfoDefinition(SKILLS_NAME, SKILLS_DESCRIPTION, secondDefinition);

        final Map<String, CustomUserInfoDefinition> userInfoDefMap = new HashMap<>(2);
        userInfoDefMap.put(firstDefinition.getName(), firstDefinition);
        userInfoDefMap.put(secondDefinition.getName(), secondDefinition);
        return userInfoDefMap;
    }

    private Map<String, CustomUserInfoDefinition> checkCustomUserInfoDefinitonsAfterUpdate() {
        final List<CustomUserInfoDefinition> customUserInfoDefinitions = getIdentityAPI()
                .getCustomUserInfoDefinitions(0, 10);
        assertThat(customUserInfoDefinitions.size()).isEqualTo(2);

        final CustomUserInfoDefinition firstDefinition = customUserInfoDefinitions.get(0);
        final CustomUserInfoDefinition secondDefinition = customUserInfoDefinitions.get(1);

        checkCustomUserInfoDefinition(LOCATION_NAME, "The office location", firstDefinition);
        checkCustomUserInfoDefinition(SKILLS_NAME, "The user skills were updated", secondDefinition);

        final Map<String, CustomUserInfoDefinition> userInfoDefMap = new HashMap<>(2);
        userInfoDefMap.put(firstDefinition.getName(), firstDefinition);
        userInfoDefMap.put(secondDefinition.getName(), secondDefinition);
        return userInfoDefMap;
    }

    private void checkCustomUserInfoDefinition(final String expectedName, final String expectedDescription,
            final CustomUserInfoDefinition customUserInfoDefinition) {
        assertThat(customUserInfoDefinition.getName()).isEqualTo(expectedName);
        assertThat(customUserInfoDefinition.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    public void reImportUserMembershipDeleted() throws Exception {
        final String userName = ANTHONY_USERNAME;

        importOrganization("simpleOrganization.xml");
        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10,
                UserMembershipCriterion.ASSIGNED_DATE_ASC);
        assertEquals(1, userMemberships.size());

        getIdentityAPI().deleteUserMembership(userMemberships.get(0).getId());

        // Re-import organization
        importOrganization("simpleOrganization.xml");
        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName);
        final List<UserMembership> userMemberships2 = getIdentityAPI().getUserMemberships(persistedUser2.getId(), 0, 10,
                UserMembershipCriterion.ASSIGNED_DATE_ASC);
        assertEquals(1, userMemberships2.size());

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    private void importOrganization(final String fileName) throws IOException, OrganizationImportException {
        try (InputStream xmlStream = OrganizationIT.class.getResourceAsStream(fileName)) {
            getIdentityAPI().importOrganization(IOUtils.toString(xmlStream, Charset.forName(UTF_8)));
        }
    }

    @Test
    public void importOrganizationWithEnabledAndDisabledUsers() throws Exception {
        // create XML file
        final String jobTitle = WEB_TEAM_MANAGER;
        final String userName = ANTHONY_USERNAME;
        final String userName1 = LIUYANYAN_USERNAME;

        importOrganization("simpleOrganization.xml");

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        assertEquals(false, persistedUser.isEnabled());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        assertEquals(true, persistedUser1.isEnabled());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteUser(persistedUser1.getId());

        final Role persistedRole = getIdentityAPI().getRoleByName(MANAGER);
        assertNotNull(persistedRole);
        getIdentityAPI().deleteRole(persistedRole.getId());
        final Role persistedRole1 = getIdentityAPI().getRoleByName(DEVELOPER);
        assertNotNull(persistedRole1);
        getIdentityAPI().deleteRole(persistedRole1.getId());

        final Group persistedGroup = getIdentityAPI().getGroupByPath(WEB_GROUP_NAME);
        assertNotNull(persistedGroup);
        getIdentityAPI().deleteGroup(persistedGroup.getId());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(ENGINE);
        assertNotNull(persistedGroup1);
        getIdentityAPI().deleteGroup(persistedGroup1.getId());
    }

    @Test
    public void importComplexOrganization() throws Exception {
        // create XML file
        final InputStream xmlStream = OrganizationIT.class.getResourceAsStream("complexOrganization.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            getIdentityAPI().importOrganization(new String(organisationContent));
        } finally {
            xmlStream.close();
        }
        final User jack = getIdentityAPI().getUserByUserName("jack");
        final User john = getIdentityAPI().getUserByUserName("john");
        assertNotSame("bpm", john.getPassword());
        assertEquals("John", john.getFirstName());
        assertEquals("Doe", john.getLastName());
        assertEquals("M", john.getTitle());
        assertEquals(jack.getId(), john.getManagerUserId());
        assertEquals(WEB_TEAM_MANAGER, john.getJobTitle());
        final ContactData johnPersoContactData = getIdentityAPI().getUserContactData(john.getId(), true);
        assertEquals("emailValue", johnPersoContactData.getEmail());
        assertEquals("phoneNumberValue", johnPersoContactData.getPhoneNumber());
        assertEquals("mobileNumberValue", johnPersoContactData.getMobileNumber());
        assertEquals("faxNumberValue", johnPersoContactData.getFaxNumber());
        assertEquals("buildingValue", johnPersoContactData.getBuilding());
        assertEquals("roomValue", johnPersoContactData.getRoom());
        assertEquals("addressValue", johnPersoContactData.getAddress());
        assertEquals("zipCodeValue", johnPersoContactData.getZipCode());
        assertEquals("cityValue", johnPersoContactData.getCity());
        assertEquals("stateValue", johnPersoContactData.getState());
        assertEquals("countryValue", johnPersoContactData.getCountry());
        assertEquals("websiteValue", johnPersoContactData.getWebsite());
        final ContactData johnProfessionalContactData = getIdentityAPI().getUserContactData(john.getId(), false);
        assertEquals("emailProfessionalValue", johnProfessionalContactData.getEmail());
        assertEquals("phoneNumberProfessionalValue", johnProfessionalContactData.getPhoneNumber());
        assertEquals("mobileNumberProfessionalValue", johnProfessionalContactData.getMobileNumber());
        assertEquals("faxNumberProfessionalValue", johnProfessionalContactData.getFaxNumber());
        assertEquals("buildingProfessionalValue", johnProfessionalContactData.getBuilding());
        assertEquals("roomProfessionalValue", johnProfessionalContactData.getRoom());
        assertEquals("addressProfessionalValue", johnProfessionalContactData.getAddress());
        assertEquals("zipCodeProfessionalValue", johnProfessionalContactData.getZipCode());
        assertEquals("cityProfessionalValue", johnProfessionalContactData.getCity());
        assertEquals("stateProfessionalValue", johnProfessionalContactData.getState());
        assertEquals("countryProfessionalValue", johnProfessionalContactData.getCountry());
        assertEquals("websiteProfessionalValue", johnProfessionalContactData.getWebsite());

        getIdentityAPI().getUserByUserName("james");

        final Group bonitaRD = getIdentityAPI().getGroupByPath("/BonitaSoft/RD");
        final Group rd = getIdentityAPI().getGroupByPath("/RD");
        getIdentityAPI().getGroupByPath("/BonitaSoft/RD");

        List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(john.getId(), 0, 10,
                UserMembershipCriterion.GROUP_NAME_ASC);
        assertEquals(1, userMemberships.size());
        UserMembership userMembership = userMemberships.get(0);
        assertEquals(bonitaRD.getId(), userMembership.getGroupId());
        assertEquals(jack.getId(), userMembership.getAssignedBy());
        assertEquals(new Date(1331142448365L), userMembership.getAssignedDate());
        userMemberships = getIdentityAPI().getUserMemberships(jack.getId(), 0, 10,
                UserMembershipCriterion.GROUP_NAME_ASC);
        assertEquals(1, userMemberships.size());
        userMembership = userMemberships.get(0);
        assertEquals(rd.getId(), userMembership.getGroupId());

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void importOrganizationWithWarnings_return_no_warnings_on_good_XML()
            throws IOException, OrganizationImportException, DeletionException {
        //given
        List<String> warnings;
        try (final InputStream xmlStream = OrganizationIT.class.getResourceAsStream("complexOrganization.xml")) {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);

            //when
            warnings = getIdentityAPI().importOrganizationWithWarnings(new String(organisationContent),
                    ImportPolicy.IGNORE_DUPLICATES);
        }

        //then
        assertThat(warnings).isEmpty();

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test(expected = GroupNotFoundException.class)
    public void importOrganizationWithWarnings_return_warnings_on_faulty_group_names()
            throws DeletionException, OrganizationImportException, IOException, GroupNotFoundException {
        //given
        List<String> warnings;
        try (final InputStream xmlStream = OrganizationIT.class
                .getResourceAsStream("complexOrganizationWithBadGroup.xml")) {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);

            //when
            warnings = getIdentityAPI().importOrganizationWithWarnings(new String(organisationContent),
                    ImportPolicy.IGNORE_DUPLICATES);
        }
        //then
        try {
            assertThat(warnings).hasSize(1);
            assertThat(warnings).contains(
                    "The group name RD/Studio contains the character '/' which is not supported. The group has not been imported");
            getIdentityAPI().getGroupByPath("/RD/Studio");
        } finally {
            // clean-up
            getIdentityAPI().deleteOrganization();
        }
    }

    @Test
    public void importOrganizationWithWarnings_imports_the_correct_groups_if_the_incorrect_one_is_present()
            throws DeletionException, OrganizationImportException, IOException, GroupNotFoundException {
        //given
        List<String> warnings;
        try (final InputStream xmlStream = OrganizationIT.class
                .getResourceAsStream("complexOrganizationWithBadGroup.xml")) {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);

            //when
            warnings = getIdentityAPI().importOrganizationWithWarnings(new String(organisationContent),
                    ImportPolicy.IGNORE_DUPLICATES);
        }
        //then
        //Should not throw any exception
        getIdentityAPI().getGroupByPath("/BonitaSoft");
        getIdentityAPI().getGroupByPath("/BonitaSoft/RD");
        getIdentityAPI().getGroupByPath("/BonitaSoft/Support");
        assertThat(getIdentityAPI().getNumberOfGroups()).isEqualTo(3);

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void importACMEOrganizationTwiceWithDefaultProfile() throws Exception {
        // create XML file

        final InputStream xmlStream = OrganizationIT.class.getResourceAsStream("ACME.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            getIdentityAPI().importOrganization(new String(organisationContent));
            getIdentityAPI().importOrganization(new String(organisationContent));
        } finally {
            xmlStream.close();
        }

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void importACMEOrganizationTwiceButRemoveGroupsAndRole() throws Exception {
        // create XML file

        final InputStream xmlStream = OrganizationIT.class.getResourceAsStream("ACME.xml");
        final byte[] organisationContent;
        try {
            organisationContent = IOUtils.toByteArray(xmlStream);
        } finally {
            xmlStream.close();
        }
        getIdentityAPI().importOrganization(new String(organisationContent));
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        // remove some groups and roles
        final List<Group> groups = getIdentityAPI().getGroups(1, 3, GroupCriterion.NAME_ASC);
        final List<Long> groupIds = new ArrayList<>(groups.size());
        for (final Group group : groups) {
            groupIds.add(group.getId());
        }
        getIdentityAPI().deleteGroups(groupIds);
        final List<Role> roles = getIdentityAPI().getRoles(0, 2, RoleCriterion.NAME_ASC);
        final List<Long> roleIds = new ArrayList<>(roles.size());
        for (final Role role : roles) {
            roleIds.add(role.getId());
        }
        getIdentityAPI().deleteRoles(roleIds);

        getIdentityAPI().importOrganization(new String(organisationContent));
        assertEquals(numberOfGroups, getIdentityAPI().getNumberOfGroups());
        assertEquals(numberOfRoles, getIdentityAPI().getNumberOfRoles());

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test(expected = InvalidOrganizationFileFormatException.class)
    public void importOrganizationWithOrganizationImportException() throws Exception {
        final String xmlOrganization = "";
        final User createUser = getIdentityAPI().createUser("john", "bpm");
        try {
            getIdentityAPI().importOrganization(xmlOrganization);
        } catch (final OrganizationImportException e) {
            // check john was not deleted:
            assertNotNull("import organization with a bad file made the organization to be deleted!",
                    getIdentityAPI().getUserByUserName("john"));
            deleteUser(createUser);
            throw e;
        }
    }

    @Test(expected = OrganizationImportException.class)
    public void importOrganizationFailRollBackToOldOrganization() throws Exception {
        InputStream xmlStream = OrganizationIT.class.getResourceAsStream("organizationFailOnDuplicates.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            final String xmlOrganization = new String(organisationContent);
            getIdentityAPI().importOrganization(xmlOrganization);

            // check john is replaced before james is imported (in the xml)
            assertTrue(xmlOrganization, xmlOrganization.indexOf("james") < xmlOrganization.indexOf("john"));
        } finally {
            xmlStream.close();
        }

        // clear organization
        getIdentityAPI().deleteOrganization();

        // ensure james does not exist anymore
        try {
            getIdentityAPI().getUserByUserName("james");
            fail("james should not be found.");
        } catch (final UserNotFoundException unfe) {
            // ok
        }

        // we have only "john" as user
        final User createUser = getIdentityAPI().createUser("john", "bpm", "John", null);

        xmlStream = OrganizationIT.class.getResourceAsStream("organizationFailOnDuplicates.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            getIdentityAPI().importOrganization(new String(organisationContent), ImportPolicy.FAIL_ON_DUPLICATES);
        } catch (final OrganizationImportException e) {
            // check john was not deleted and have old username
            try {
                getIdentityAPI().getUserByUserName("james");
                fail("old organisation should be restored");
            } catch (final UserNotFoundException unfe) {
                deleteUser(createUser);
                throw e;
            }
        } finally {
            xmlStream.close();
        }

    }

    @Test
    public void importOrganizationWithCycle() throws Exception {
        final InputStream xmlStream = OrganizationIT.class.getResourceAsStream("organizationWithCycle.xml");
        try {
            final byte[] bs = IOUtils.toByteArray(xmlStream);
            final String organizationContent = new String(bs);
            getIdentityAPI().importOrganization(organizationContent);

            // clean-up
            getIdentityAPI().deleteOrganization();
            getIdentityAPI().importOrganization(organizationContent);
        } finally {
            xmlStream.close();
        }

        final List<User> users = getIdentityAPI().getUsers(0, 10, UserCriterion.USER_NAME_ASC);
        assertThat(users).extracting("userName", "managerUserId").containsExactly(tuple("user1", users.get(1).getId()),
                tuple("user2", users.get(2).getId()),
                tuple("user3", users.get(0).getId()));
        assertEquals(3, users.size());

        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void deleteOrganization() throws Exception {
        // Create records for user role, group and membership
        final User persistedUser1 = getIdentityAPI().createUser(LIUYANYAN_USERNAME, "bpm");
        final User persistedUser2 = getIdentityAPI().createUser(ANTHONY_USERNAME, "bpm");

        final RoleCreator rc1 = new RoleCreator(DEVELOPER);
        rc1.setDisplayName("roleDisplayName");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator(MANAGER);
        rc2.setDisplayName("roleDisplayName");
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator(ENGINE);
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator(WEB_GROUP_NAME);
        groupCreator2.setDisplayName(WEB_TEAM);
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        getIdentityAPI().addUserMembership(persistedUser1.getId(), persistedGroup1.getId(), persistedRole1.getId());
        getIdentityAPI().addUserMembership(persistedUser2.getId(), persistedGroup2.getId(), persistedRole2.getId());

        getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("ToDelete"));

        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(persistedUser1.getId()));
        assertEquals(2, getIdentityAPI().getNumberOfGroups());
        assertEquals(2, getIdentityAPI().getNumberOfUsers());
        assertEquals(2, getIdentityAPI().getNumberOfRoles());

        // Create process that is mapped to user of organization
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("deleteAllHuman", "1.1").addActor(ACTOR_NAME, true).addUserTask("human", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME,
                persistedUser1);
        final StartProcessUntilStep startProcessUntilStep = startProcessAndWaitForTask(processDefinition.getId(),
                "human");
        assignAndExecuteStep(startProcessUntilStep.getActivityInstance(), persistedUser1.getId());
        waitForProcessToFinish(startProcessUntilStep.getProcessInstance());

        // delete organization and do check
        getIdentityAPI().deleteOrganization();
        assertEquals(0, getIdentityAPI().getNumberOfGroups());
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
        assertEquals(0, getIdentityAPI().getNumberOfRoles());
        assertEquals(0, getIdentityAPI().getNumberOfUserMemberships(persistedUser1.getId()));
        assertThat(getIdentityAPI().getCustomUserInfoDefinitions(0, 10)).isEmpty();

        // reload the process deploy info:
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI()
                .getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = DeletionException.class)
    public void cantDeleteOrganizationWhenProcessInstanceIsActive() throws Exception {
        // Create records for user role, group and membership
        final User persistedUser1 = getIdentityAPI().createUser(LIUYANYAN_USERNAME, "bpm");
        final User persistedUser2 = getIdentityAPI().createUser(ANTHONY_USERNAME, "bpm");

        final RoleCreator rc1 = new RoleCreator(DEVELOPER);
        rc1.setDisplayName("roleDisplayName");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator(MANAGER);
        rc2.setDisplayName("roleDisplayName");
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator(ENGINE);
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator(WEB_GROUP_NAME);
        groupCreator2.setDisplayName(WEB_TEAM);
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        getIdentityAPI().addUserMembership(persistedUser1.getId(), persistedGroup1.getId(), persistedRole1.getId());
        getIdentityAPI().addUserMembership(persistedUser2.getId(), persistedGroup2.getId(), persistedRole2.getId());

        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(persistedUser1.getId()));
        assertEquals(2, getIdentityAPI().getNumberOfGroups());
        assertEquals(2, getIdentityAPI().getNumberOfUsers());
        assertEquals(2, getIdentityAPI().getNumberOfRoles());

        // Create process that is mapped to user of organization
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("deleteAllHuman", "1.1").addActor(ACTOR_NAME, true).addUserTask("human", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME,
                persistedUser1);
        final StartProcessUntilStep startProcessUntilStep = startProcessAndWaitForTask(processDefinition.getId(),
                "human");

        // delete organization and do check
        try {
            getIdentityAPI().deleteOrganization();
        } finally {
            // Clean up
            assignAndExecuteStep(startProcessUntilStep.getActivityInstance(), persistedUser1.getId());
            waitForProcessToFinish(startProcessUntilStep.getProcessInstance());
            getIdentityAPI().deleteOrganization();
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test(expected = OrganizationImportException.class)
    public void importOrganizationFailOnDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.FAIL_ON_DUPLICATES;

        final String userName = ANTHONY_USERNAME;
        final String jobTitle = WEB_TEAM_MANAGER;
        final String roleName = MANAGER;
        final String roleDisplayName = BONITA_MANAGER;
        final String groupName = WEB_GROUP_NAME;
        final String groupDisplayName = WEB_TEAM;

        final String userName1 = LIUYANYAN_USERNAME;
        final String roleName1 = DEVELOPER;
        final String roleDisplayName1 = "Bonita developer";
        final String groupName1 = ENGINE;
        final String groupDisplayName1 = "engine team";

        importAndCheckFirstSimpleOrganization();
        final User userToDisable = getIdentityAPI().getUserByUserName(userName);
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(false);
        getIdentityAPI().updateUser(userToDisable.getId(), updateDescriptor);
        assertEquals(2, getIdentityAPI().getNumberOfUsers());
        try {
            importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", policy);
        } catch (final OrganizationImportException e) {

            assertEquals(2, getIdentityAPI().getNumberOfUsers());
            assertEquals(2, getIdentityAPI().getNumberOfGroups());
            assertEquals(2, getIdentityAPI().getNumberOfRoles());

            final User persistedUser = getIdentityAPI().getUserByUserName(userName);
            assertNotNull(persistedUser);
            assertEquals(jobTitle, persistedUser.getJobTitle());
            assertFalse(persistedUser.isEnabled());
            final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
            assertNotNull(persistedRole);
            assertEquals(roleDisplayName, persistedRole.getDisplayName());
            final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
            assertNotNull(persistedGroup);
            assertEquals(groupDisplayName, persistedGroup.getDisplayName());
            final UserMembership persistedMembership = getIdentityAPI()
                    .getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC).get(0);
            assertEquals(groupName, persistedMembership.getGroupName());
            assertEquals(roleName, persistedMembership.getRoleName());
            assertEquals(userName, persistedMembership.getUsername());
            // check assignedBy for membership
            assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

            final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
            assertNotNull(persistedUser1);
            assertTrue(persistedUser1.isEnabled());
            final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
            assertNotNull(persistedRole1);
            assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
            final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
            assertNotNull(persistedGroup1);
            assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
            final UserMembership persistedMembership1 = getIdentityAPI()
                    .getUserMemberships(persistedUser1.getId(), 0, 10,
                            UserMembershipCriterion.GROUP_NAME_ASC)
                    .get(0);
            assertEquals(groupName1, persistedMembership1.getGroupName());
            assertEquals(roleName1, persistedMembership1.getRoleName());
            assertEquals(userName1, persistedMembership1.getUsername());
            // check assignedBy for membership
            assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

            // clean-up
            getIdentityAPI().deleteUser(persistedUser.getId());
            getIdentityAPI().deleteRole(persistedRole.getId());
            getIdentityAPI().deleteGroup(persistedGroup.getId());

            getIdentityAPI().deleteUser(persistedUser1.getId());
            getIdentityAPI().deleteRole(persistedRole1.getId());
            getIdentityAPI().deleteGroup(persistedGroup1.getId());
            throw e;
        }

        fail("This statement shouldn't be reached.");
    }

    @Test
    public void importOrganizationFailOnDuplicatesNoDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.FAIL_ON_DUPLICATES;

        final String userName = ANTHONY_USERNAME;
        final String jobTitle = WEB_TEAM_MANAGER;
        final String roleName = MANAGER;
        final String roleDisplayName = BONITA_MANAGER;
        final String groupName = WEB_GROUP_NAME;
        final String groupDisplayName = WEB_TEAM;

        final String userName1 = LIUYANYAN_USERNAME;
        final String roleName1 = DEVELOPER;
        final String roleDisplayName1 = "Bonita developer";
        final String groupName1 = ENGINE;
        final String groupDisplayName1 = "engine team";

        final String userName2 = JOHNNYFOOTBALL;
        final String roleName2 = "Tester";
        final String groupName2 = QA;

        importAndCheckFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationNoDuplicates.xml", policy);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI()
                .getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
        assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
        final UserMembership persistedMembership1 = getIdentityAPI()
                .getUserMemberships(persistedUser1.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName1, persistedMembership1.getGroupName());
        assertEquals(roleName1, persistedMembership1.getRoleName());
        assertEquals(userName1, persistedMembership1.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName2);
        assertNotNull(persistedUser2);
        final Role persistedRole2 = getIdentityAPI().getRoleByName(roleName2);
        assertNotNull(persistedRole2);
        final Group persistedGroup2 = getIdentityAPI().getGroupByPath(groupName2);
        assertNotNull(persistedGroup2);
        final UserMembership persistedMembership2 = getIdentityAPI()
                .getUserMemberships(persistedUser2.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName2, persistedMembership2.getGroupName());
        assertEquals(roleName2, persistedMembership2.getRoleName());
        assertEquals(userName2, persistedMembership2.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership2.getAssignedBy());

        // clean-up
        getIdentityAPI().deleteOrganization();

    }

    @Test
    public void importOrganizationMergeDuplicates() throws Exception {
        // given
        final ImportPolicy policy = ImportPolicy.MERGE_DUPLICATES;
        importAndCheckFirstSimpleOrganization();

        // when
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", policy);

        // then
        final Map<String, CustomUserInfoDefinition> infoDefinitonsAfterUpdate = checkCustomUserInfoDefinitonsAfterUpdate();
        checkUsersAfterUpdate();
        checkCustomUserInfoValuesAfterUpdate(infoDefinitonsAfterUpdate);
        checkGroupsAfterUpdate();
        checkRolesAfterUpdate();
        checkMembershipAfterUpdate();

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    private void checkMembershipAfterUpdate() throws UserNotFoundException {
        final User persistedUser = getIdentityAPI().getUserByUserName(ANTHONY_USERNAME);
        final UserMembership persistedMembership = getIdentityAPI()
                .getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(WEB_GROUP_NAME, persistedMembership.getGroupName());
        assertEquals(MANAGER, persistedMembership.getRoleName());
        assertEquals(ANTHONY_USERNAME, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        final UserMembership persistedMembership1 = getIdentityAPI()
                .getUserMemberships(persistedUser1.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(ENGINE, persistedMembership1.getGroupName());
        assertEquals(DEVELOPER, persistedMembership1.getRoleName());
        assertEquals(LIUYANYAN_USERNAME, persistedMembership1.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

        final User persistedUser2 = getIdentityAPI().getUserByUserName(JOHNNYFOOTBALL);
        final UserMembership persistedMembership2 = getIdentityAPI()
                .getUserMemberships(persistedUser2.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(QA, persistedMembership2.getGroupName());
        assertEquals("Tester", persistedMembership2.getRoleName());
        assertEquals(JOHNNYFOOTBALL, persistedMembership2.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership2.getAssignedBy());
    }

    private void checkRolesAfterUpdate() throws RoleNotFoundException {
        final Role persistedRole = getIdentityAPI().getRoleByName(MANAGER);
        assertEquals(BONITA_MANAGER, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());

        final Role persistedRole1 = getIdentityAPI().getRoleByName(DEVELOPER);
        assertNotNull(persistedRole1);
        assertEquals("Bonitasoft developer", persistedRole1.getDisplayName());

        final Role persistedRole2 = getIdentityAPI().getRoleByName("Tester");
        assertNotNull(persistedRole2);
    }

    private void checkGroupsAfterUpdate() throws GroupNotFoundException {
        final Group persistedGroup = getIdentityAPI().getGroupByPath(WEB_GROUP_NAME);
        assertEquals(WEB_TEAM, persistedGroup.getDisplayName());
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());

        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(ENGINE);
        assertNotNull(persistedGroup1);
        assertEquals("RD engine team", persistedGroup1.getDisplayName());

        final Group persistedGroup2 = getIdentityAPI().getGroupByPath(QA);
        assertNotNull(persistedGroup2);
    }

    private void checkUsersAfterUpdate() throws UserNotFoundException {
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        final User persistedUser = getIdentityAPI().getUserByUserName(ANTHONY_USERNAME);
        assertEquals(WEB_TEAM_MANAGER, persistedUser.getJobTitle());
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        assertNotNull(persistedUser1);

        final User persistedUser2 = getIdentityAPI().getUserByUserName(JOHNNYFOOTBALL);
        assertNotNull(persistedUser2);
    }

    @Test
    public void importOrganizationMergeDuplicatesWithEnabledAndDisabledUsers() throws Exception {
        // create XML file
        importAndCheckFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", ImportPolicy.MERGE_DUPLICATES);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(JOHNNYFOOTBALL);
        assertNotNull(persistedUser);
        assertEquals(false, persistedUser.isEnabled());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        assertNotNull(persistedUser1);
        assertEquals(true, persistedUser1.isEnabled());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteUser(persistedUser1.getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Tester").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath(QA).getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName(DEVELOPER).getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath(ENGINE).getId());

        getIdentityAPI().deleteUser(getIdentityAPI().getUserByUserName(ANTHONY_USERNAME).getId());
        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName(MANAGER).getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath(WEB_GROUP_NAME).getId());
    }

    @Test
    public void importOrganizationIgnoreDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.IGNORE_DUPLICATES;

        final String userName = ANTHONY_USERNAME;
        final String jobTitle = WEB_TEAM_MANAGER;
        final String roleName = MANAGER;
        final String roleDisplayName = BONITA_MANAGER;
        final String groupName = WEB_GROUP_NAME;
        final String groupDisplayName = WEB_TEAM;

        final String userName1 = LIUYANYAN_USERNAME;
        final String roleName1 = DEVELOPER;
        final String roleDisplayName1 = "Bonita developer";
        final String groupName1 = ENGINE;
        final String groupDisplayName1 = "engine team";

        final String userName2 = JOHNNYFOOTBALL;
        final String roleName2 = "Tester";
        final String groupName2 = QA;

        importAndCheckFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", policy);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI()
                .getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
        assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
        final UserMembership persistedMembership1 = getIdentityAPI()
                .getUserMemberships(persistedUser1.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName1, persistedMembership1.getGroupName());
        assertEquals(roleName1, persistedMembership1.getRoleName());
        assertEquals(userName1, persistedMembership1.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName2);
        assertNotNull(persistedUser2);
        final Role persistedRole2 = getIdentityAPI().getRoleByName(roleName2);
        assertNotNull(persistedRole2);
        final Group persistedGroup2 = getIdentityAPI().getGroupByPath(groupName2);
        assertNotNull(persistedGroup2);
        final UserMembership persistedMembership2 = getIdentityAPI()
                .getUserMemberships(persistedUser2.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName2, persistedMembership2.getGroupName());
        assertEquals(roleName2, persistedMembership2.getRoleName());
        assertEquals(userName2, persistedMembership2.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership2.getAssignedBy());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteRole(persistedRole.getId());
        getIdentityAPI().deleteGroup(persistedGroup.getId());

        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());

        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());
    }

    @Test
    public void importOrganizationIgnoreDuplicatesWithEnabledAndDisabledUsers() throws Exception {
        // create XML file
        importAndCheckFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", ImportPolicy.IGNORE_DUPLICATES);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(JOHNNYFOOTBALL);
        assertNotNull(persistedUser);
        assertFalse(persistedUser.isEnabled());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        assertNotNull(persistedUser1);
        assertTrue(persistedUser1.isEnabled());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteUser(persistedUser1.getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Tester").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath(QA).getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName(DEVELOPER).getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath(ENGINE).getId());

        getIdentityAPI().deleteUser(getIdentityAPI().getUserByUserName(ANTHONY_USERNAME).getId());
        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName(MANAGER).getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath(WEB_GROUP_NAME).getId());
    }

    private void importOrganizationWithPolicy(final String xmlFile, final ImportPolicy policy) throws Exception {
        final InputStream xmlStream = OrganizationIT.class.getResourceAsStream(xmlFile);
        try {
            final String organizationContent = IOUtils.toString(xmlStream);
            getIdentityAPI().importOrganization(organizationContent, policy);
        } finally {
            xmlStream.close();
        }
    }

    private void importAndCheckFirstSimpleOrganization() throws Exception {
        // when
        importOrganization("simpleOrganizationDuplicates1.xml");

        // then
        final Map<String, CustomUserInfoDefinition> userInfoDefinitons = checkDefaultCustomUserInfoDefinitons();
        checkDefaultUsers();
        checkDefaultCustomUserInfoValues(userInfoDefinitons);
        checkDefaultGroups();
        checkDefaultRoles();
        checkDefaultMembership();

    }

    private void checkDefaultMembership() throws UserNotFoundException {
        final User persistedUser = getIdentityAPI().getUserByUserName(ANTHONY_USERNAME);
        final UserMembership persistedMembership = getIdentityAPI()
                .getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(WEB_GROUP_NAME, persistedMembership.getGroupName());
        assertEquals(MANAGER, persistedMembership.getRoleName());
        assertEquals(ANTHONY_USERNAME, persistedMembership.getUsername());
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());
    }

    private void checkDefaultRoles() throws RoleNotFoundException {
        final Role persistedRole = getIdentityAPI().getRoleByName(MANAGER);
        assertEquals(BONITA_MANAGER, persistedRole.getDisplayName());
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());

        final Role persistedRole1 = getIdentityAPI().getRoleByName(DEVELOPER);
        assertNotNull(persistedRole1);
    }

    private void checkDefaultGroups() throws GroupNotFoundException {
        final Group persistedGroup = getIdentityAPI().getGroupByPath(WEB_GROUP_NAME);
        assertEquals(WEB_TEAM, persistedGroup.getDisplayName());
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());

        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(ENGINE);
        assertNotNull(persistedGroup1);
    }

    private void checkDefaultUsers() throws UserNotFoundException {
        final User persistedUser = getIdentityAPI().getUserByUserName(ANTHONY_USERNAME);
        assertEquals(WEB_TEAM_MANAGER, persistedUser.getJobTitle());
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(LIUYANYAN_USERNAME);
        assertNotNull(persistedUser1);
    }

    @Test
    public void exportOrganization() throws Exception {
        // create records for user role, group and membership
        // users
        final User persistedUser1 = getIdentityAPI().createUser(LIUYANYAN_USERNAME, "bpm");

        final UserCreator creator = new UserCreator(ANTHONY_USERNAME, "bpm");
        creator.setJobTitle(WEB_TEAM_MANAGER);
        final User persistedUser2 = getIdentityAPI().createUser(creator);

        // roles
        final RoleCreator rc1 = new RoleCreator(DEVELOPER);
        rc1.setDisplayName("Bonita developer");
        rc1.setIcon("myIcon.jpg", new byte[] { 1, 2, 3 });
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator(MANAGER);
        rc2.setDisplayName(BONITA_MANAGER);
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        // groups
        final GroupCreator groupCreator1 = new GroupCreator(ENGINE);
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator(WEB_GROUP_NAME);
        groupCreator2.setDisplayName(WEB_TEAM);
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        // membership
        final UserMembership membership1 = getIdentityAPI().addUserMembership(persistedUser1.getId(),
                persistedGroup1.getId(), persistedRole1.getId());
        final UserMembership membership2 = getIdentityAPI().addUserMembership(persistedUser2.getId(),
                persistedGroup2.getId(), persistedRole2.getId());

        // custom user info definition
        final CustomUserInfoDefinition skills = getIdentityAPI().createCustomUserInfoDefinition(
                new CustomUserInfoDefinitionCreator(SKILLS_NAME, SKILLS_DESCRIPTION));
        getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator(LOCATION_NAME));

        // custom user info value
        getIdentityAPI().setCustomUserInfoValue(skills.getId(), persistedUser1.getId(), SKILLS_VALUE);

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        assertThat(organizationContent).contains(DEVELOPER);
        assertThat(organizationContent).contains("Bonita developer");
        assertThat(organizationContent).contains(ENGINE);
        //Icon name and Icon path are not exported (deprecated)
        assertThat(organizationContent).doesNotContain("<iconName/>");
        assertThat(organizationContent).doesNotContain("<iconPath/>");
        assertThat(organizationContent).contains("engine team");
        assertThat(organizationContent)
                .contains(getIdentityAPI().getUserMembership(membership1.getId()).getGroupName());
        assertThat(organizationContent)
                .contains(getIdentityAPI().getUserMembership(membership2.getId()).getGroupName());
        assertThat(organizationContent).contains(SKILLS_NAME);
        assertThat(organizationContent).contains(SKILLS_DESCRIPTION);
        assertThat(organizationContent).contains(LOCATION_NAME);
        assertThat(organizationContent).contains(SKILLS_VALUE);

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void exportOrganizationWithSpecialCharacters() throws Exception {
        // create records for user role, group and membership
        final User user = getIdentityAPI().createUser("Céline*^$", "ploµ¨µ%§");
        final User user2 = getIdentityAPI().createUser("ééééééééééééééééééé", "éééééééééééééééé");

        final RoleCreator roleCreator = new RoleCreator("Développeur");
        roleCreator.setDisplayName("'(-è");
        roleCreator.setDescription("è-__ç_");
        roleCreator.setIconName("(-è_");
        roleCreator.setIconPath("^*_ç");
        final Role role = getIdentityAPI().createRole(roleCreator);

        final GroupCreator groupCreator = new GroupCreator("µ£¨µ");
        groupCreator.setDisplayName(".?/5434%¨%¨%");
        groupCreator.setDescription("è-__ç_2");
        groupCreator.setIconName("(-è_2");
        groupCreator.setIconPath("^*_ç2");
        groupCreator.setParentPath("$*ù$^ù");
        final Group group = getIdentityAPI().createGroup(groupCreator);

        final UserMembership membership = getIdentityAPI().addUserMembership(user.getId(), group.getId(), role.getId());

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        // Role
        for (final Entry<RoleCreator.RoleField, Serializable> entry : roleCreator.getFields().entrySet()) {
            assertThat(organizationContent).contains((String) entry.getValue());
        }

        // Group
        for (final Entry<GroupField, Serializable> entry : groupCreator.getFields().entrySet()) {
            assertThat(organizationContent).contains((String) entry.getValue());
        }

        // User
        assertThat(organizationContent).contains("Céline*^$");
        assertThat(organizationContent).contains("ééééééééééééééééééé");

        // UserMembership
        assertThat(organizationContent).contains(getIdentityAPI().getUserMembership(membership.getId()).getGroupName());

        // Verify all tags
        assertThat(organizationContent).contains("<organization:Organization");
        assertThat(organizationContent).contains("<users>");
        assertThat(organizationContent).contains("<user");
        assertThat(organizationContent).contains("</user>");
        assertThat(organizationContent).contains("</users>");
        assertThat(organizationContent).contains("<roles>");
        assertThat(organizationContent).contains("<role");
        assertThat(organizationContent).contains("</role>");
        assertThat(organizationContent).contains("</roles>");
        assertThat(organizationContent).contains("<groups>");
        assertThat(organizationContent).contains("<group");
        assertThat(organizationContent).contains("</group>");
        assertThat(organizationContent).contains(" </groups>");
        assertThat(organizationContent).contains("<memberships");
        assertThat(organizationContent).contains("</memberships>");
        assertThat(organizationContent).contains("</organization:Organization>");

        // clean-up
        deleteUsers(user, user2);
        getIdentityAPI().deleteRole(role.getId());
        getIdentityAPI().deleteGroup(group.getId());
    }

    @Test
    public void importAndExportOrganizationWithSpecialCharacters() throws Exception {
        importOrganization("OrganizationWithSpecialCharacters.xml");

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        // Role
        assertThat(organizationContent).contains("ééé");

        // Group
        assertThat(organizationContent).contains("ééééééééé");

        // User
        assertThat(organizationContent).contains("éé");

        // Verify all tags
        assertThat(organizationContent).contains("<organization:Organization");
        assertThat(organizationContent).contains("<users>");
        assertThat(organizationContent).contains("<user");
        assertThat(organizationContent).contains("</user>");
        assertThat(organizationContent).contains("</users>");
        assertThat(organizationContent).contains("<roles>");
        assertThat(organizationContent).contains("<role");
        assertThat(organizationContent).contains("</role>");
        assertThat(organizationContent).contains("</roles>");
        assertThat(organizationContent).contains("<groups>");
        assertThat(organizationContent).contains("<group");
        assertThat(organizationContent).contains("</group>");
        assertThat(organizationContent).contains(" </groups>");
        assertThat(organizationContent).contains("<memberships/>");
        assertThat(organizationContent).contains("</organization:Organization>");

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void exportAndImportOrganization() throws Exception {
        // create records for user role, group and membership
        final String username = LIUYANYAN_USERNAME;
        final String password = "bpm";
        final User persistedUser1 = getIdentityAPI().createUser(username, password);

        final UserCreator creator = new UserCreator(ANTHONY_USERNAME, password);
        creator.setJobTitle(WEB_TEAM_MANAGER);
        final User persistedUser2 = getIdentityAPI().createUser(creator);

        final RoleCreator rc1 = new RoleCreator(DEVELOPER);
        rc1.setDisplayName("Bonita developer");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator(MANAGER);
        rc2.setDisplayName(BONITA_MANAGER);
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator(ENGINE);
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator(WEB_GROUP_NAME);
        groupCreator2.setDisplayName(WEB_TEAM);
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        final UserMembership membership1 = getIdentityAPI().addUserMembership(persistedUser1.getId(),
                persistedGroup1.getId(), persistedRole1.getId());
        final UserMembership membership2 = getIdentityAPI().addUserMembership(persistedUser2.getId(),
                persistedGroup2.getId(), persistedRole2.getId());

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        assertThat(organizationContent).contains(DEVELOPER);
        assertThat(organizationContent).contains("Bonita developer");
        assertThat(organizationContent).contains(ENGINE);
        assertThat(organizationContent).contains("engine team");
        assertThat(organizationContent)
                .contains(getIdentityAPI().getUserMembership(membership1.getId()).getGroupName());
        assertThat(organizationContent)
                .contains(getIdentityAPI().getUserMembership(membership2.getId()).getGroupName());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());

        getIdentityAPI().importOrganization(organizationContent);
        final List<User> users = getIdentityAPI().getUsers(0, 10, UserCriterion.FIRST_NAME_ASC);
        for (final User user : users) {
            getIdentityAPI().deleteUser(user.getId());
        }
        assertEquals(2, users.size());
        final List<Role> roles = getIdentityAPI().getRoles(0, 10, RoleCriterion.NAME_ASC);
        for (final Role role : roles) {
            getIdentityAPI().deleteRole(role.getId());
        }
        final List<Group> groups = getIdentityAPI().getGroups(0, 10, GroupCriterion.NAME_ASC);
        for (final Group group : groups) {
            getIdentityAPI().deleteGroup(group.getId());
        }
    }

    @Test
    public void exportOrganizationWithDisabledUsers() throws Exception {
        // create records for user
        final User persistedUser = getIdentityAPI().createUser(LIUYANYAN_USERNAME, "bpm");
        final UserUpdater updater = new UserUpdater();
        updater.setEnabled(false);
        getIdentityAPI().updateUser(persistedUser.getId(), updater);

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();
        assertThat(organizationContent).contains("false");

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
    }

    @Test
    public void exportOrganizationWithEnabledUsers() throws Exception {
        // create records for user
        final UserCreator creator = new UserCreator(ANTHONY_USERNAME, "bpm");
        creator.setEnabled(true);
        final User persistedUser = getIdentityAPI().createUser(creator);

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();
        assertThat(organizationContent).contains("true");

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
    }

    @Test
    public void should_import_manager_of_user() throws Exception {
        //given
        User john = getIdentityAPI().createUser("john", "bpm");
        //manager is created after the user to ensure it does not depend of user import order
        User johnManager = getIdentityAPI().createUser("johnManager", "bpm");
        getIdentityAPI().updateUser(john.getId(), new UserUpdater().setManagerId(johnManager.getId()));
        String organization = getIdentityAPI().exportOrganization();
        getIdentityAPI().deleteOrganization();
        //when
        getIdentityAPI().importOrganization(organization);
        //then
        assertThat(getIdentityAPI().getUserByUserName("john").getManagerUserId())
                .as("manager id of john").isEqualTo(getIdentityAPI().getUserByUserName("johnManager").getId());
        //clean
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void should_import_update_manager_of_user() throws Exception {
        //given
        User john = getIdentityAPI().createUser("john", "bpm");
        //manager is created after the user to ensure it does not depend of user import order
        User johnManager = getIdentityAPI().createUser("johnManager", "bpm");
        getIdentityAPI().updateUser(john.getId(), new UserUpdater().setManagerId(johnManager.getId()));
        User newJohnManager = getIdentityAPI().createUser("newjohnManager", "bpm");
        String organization = getIdentityAPI().exportOrganization();
        getIdentityAPI().updateUser(john.getId(), new UserUpdater().setManagerId(newJohnManager.getId()));
        getIdentityAPI().deleteUser(johnManager.getId());
        //when
        getIdentityAPI().importOrganization(organization);
        //then
        assertThat(getIdentityAPI().getUserByUserName("john").getManagerUserId())
                .as("manager id of john").isEqualTo(getIdentityAPI().getUserByUserName("johnManager").getId());
        //clean
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void should_import_user_even_if_manager_is_unkown() throws Exception {
        //given
        User john = getIdentityAPI().createUser("john", "bpm");
        //manager is created after the user to ensure it does not depend of user import order
        User johnManager = getIdentityAPI().createUser("johnManager", "bpm");
        getIdentityAPI().updateUser(john.getId(), new UserUpdater().setManagerId(johnManager.getId()));
        String organization = getIdentityAPI().exportOrganization();
        getIdentityAPI().deleteOrganization();
        //when
        systemOutRule.enableLog();
        getIdentityAPI().importOrganization(removeJohnManagerNode(organization));
        //then
        assertThat(getIdentityAPI().getUserByUserName("john").getManagerUserId())
                .as("manager id of john").isEqualTo(0);
        assertThat(systemOutRule.getLog())
                .contains(
                        "The user john has a manager with username johnManager, but this one does not exist. Please set it manually.");
        //clean
        getIdentityAPI().deleteOrganization();
    }

    private String removeJohnManagerNode(String organization) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new StringReader(organization)));
        Node organizationNode = doc.getFirstChild();
        Node users = organizationNode.getChildNodes().item(3);
        if (users.getChildNodes().item(3).getAttributes().item(0).getNodeValue().equals("johnManager")) {
            users.removeChild(users.getChildNodes().item(3));
        } else {
            users.removeChild(users.getChildNodes().item(1));
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

}
