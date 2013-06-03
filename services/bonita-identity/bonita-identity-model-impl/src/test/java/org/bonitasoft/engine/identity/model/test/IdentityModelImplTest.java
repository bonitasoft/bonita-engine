/**
 * Copyright (C) 2011  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.bonitasoft.engine.identity.model.test;

import org.bonitasoft.engine.identity.model.builder.impl.IdentityModelBuilderImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class IdentityModelImplTest {


  private final IdentityModelBuilderImpl builder;

  private final static Logger LOGGER = LoggerFactory.getLogger(IdentityModelImplTest.class);

  public IdentityModelImplTest() {
    this.builder = new IdentityModelBuilderImpl();
  }

  @Rule
  public TestName name = new TestName();

  @Before
  public void setUp() throws Exception {
    LOGGER.info("Testing : {}", this.name.getMethodName());
  }

  @After
  public void tearDown() throws Exception {
    LOGGER.info("Tested: {}", this.name.getMethodName());
  }

//  @Test
//  public void testBuildUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id)
//        .setUsername("username").setFirstName("firstName").setLastName("lastName").setPassword("password")
//        .setTitle("title").setJobTitle("jobTitle").setManagerID(id + 1l).setDelegeeID(id + 2l).done();
//
//    assertNotNull(user);
//    assertEquals(id, user.getId());
//    assertEquals("username", user.getUserName());
//    assertEquals("firstName", user.getFirstName());
//    assertEquals("lastName", user.getLastName());
//    assertEquals("password", user.getPassword());
//    assertEquals("title", user.getTitle());
//    assertEquals("jobTitle", user.getJobTitle());
//    assertEquals(id + 1l, user.getManagerID());
//    assertEquals(id + 2l, user.getDelegeeID());
//  }
//
//  @Test
//  public void testBuildUserWithPersonalContactInfo() {
//    final long id = new Date().getTime();
//    final SContactInfo contactInfo = this.builder.getContactInfoBuilder().createNewInstance().setAddress("adresse")
//        .setBuilding("building").setCity("city").setCountry("country").setEmail("email").setFaxNumber("fax")
//        .setId(id + 1l).setMobileNumber("mobile").setPhoneNumber("phone").setState("state").setWebsite("website")
//        .setZipCode("zipcode").done();
//
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id)
//        .setPersonalContactInfo(contactInfo).done();
//
//    assertNotNull(user);
//    assertEquals(id, user.getId());
//    assertEquals(contactInfo, user.getPersonalContactInfoId());
//  }
//
//  @Test
//  public void testBuildUserWithProfessionalContactInfo() {
//    final long id = new Date().getTime();
//    final SContactInfo contactInfo = this.builder.getContactInfoBuilder().createNewInstance().setAddress("adresse")
//        .setBuilding("building").setCity("city").setCountry("country").setEmail("email").setFaxNumber("fax")
//        .setId(id + 1l).setMobileNumber("mobile").setPhoneNumber("phone").setState("state").setWebsite("website")
//        .setZipCode("zipcode").done();
//
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id)
//        .setProfessionalContactInfo(contactInfo).done();
//
//    assertNotNull(user);
//    assertEquals(id, user.getId());
//    final SContactInfo professionalContactInfo = user.getProfessionalContactInfoId();
//    assertEquals(contactInfo, professionalContactInfo);
//
//    assertEquals("adresse", professionalContactInfo.getAddress());
//    assertEquals("building", professionalContactInfo.getBuilding());
//    assertEquals("city", professionalContactInfo.getCity());
//    assertEquals("country", professionalContactInfo.getCountry());
//    assertEquals("email", professionalContactInfo.getEmail());
//    assertEquals("fax", professionalContactInfo.getFaxNumber());
//    assertEquals(id + 1l, professionalContactInfo.getId());
//    assertEquals("mobile", professionalContactInfo.getMobileNumber());
//    assertEquals("phone", professionalContactInfo.getPhoneNumber());
//    assertEquals("state", professionalContactInfo.getState());
//    assertEquals("website", professionalContactInfo.getWebsite());
//    assertEquals("zipcode", professionalContactInfo.getZipCode());
//  }
//
//  @Test
//  public void testBuildRole() {
//    final long id = new Date().getTime();
//
//    final SRoleImpl role = (SRoleImpl) this.builder.getRoleBuilder().createNewInstance().setId(id).setLabel("label")
//        .setName("name").setDescription("description").done();
//
//    assertNotNull(role);
//    assertEquals(id, role.getId());
//    assertEquals("label", role.getLabel());
//    assertEquals("name", role.getName());
//    assertEquals("description", role.getDescription());
//  }
//
//  @Test
//  public void testBuildGroup() {
//    final long id = new Date().getTime();
//    final SGroupImpl parentGroup = (SGroupImpl) this.builder.getGroupBuilder().createNewInstance().setId(id + 1l)
//        .done();
//    final SGroupImpl group = (SGroupImpl) this.builder.getGroupBuilder().createNewInstance().setId(id)
//        .setLabel("label").setName("name").setDescription("description").setParentGroupId(parentGroup.getId()).done();
//
//    assertNotNull(group);
//    assertEquals(id, group.getId());
//    assertEquals("label", group.getLabel());
//    assertEquals("name", group.getName());
//    assertEquals("description", group.getDescription());
//    assertEquals(parentGroup, group.getParentGroupId());
//  }
//
//  @Test
//  public void testBuildProfileMetadata() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1)
//        .setLabel("label").setName("name").setDescription("description").addUser(user, "theMetadata").done();
//
//    assertNotNull(metadata);
//    assertEquals(id + 1l, metadata.getId());
//    assertEquals("label", metadata.getLabel());
//    assertEquals("name", metadata.getName());
//    assertEquals("description", metadata.getDescription());
//    assertTrue(metadata.getUsers().containsKey(user));
//    assertEquals("theMetadata", metadata.getUsers().get(user));
//  }
//
//  @Test
//  public void testBuildMembership() {
//    final long id = new Date().getTime();
//    final SGroup group = this.builder.getGroupBuilder().createNewInstance().setId(id).done();
//    final SRole role = this.builder.getRoleBuilder().createNewInstance().setId(id + 1).done();
//    final SMembership membership = this.builder.getMembershipBuilder().createNewInstance().setId(id + 2).setRole(role)
//        .setGroup(group).done();
//
//    assertNotNull(membership);
//    assertEquals(id + 2l, membership.getId());
//    assertEquals(role, membership.getRoleId());
//    assertEquals(group, membership.getGroupId());
//  }
//
//  @Test
//  public void testAddMembershipToUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SMembership membership = this.builder.getMembershipBuilder().createNewInstance().setId(id + 1l).done();
//
//    assertFalse(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 0);
//    user.addToMemberships(membership);
//    assertTrue(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 1);
//  }
//
//  @Test
//  public void testRemoveMembershipFromUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SMembership membership = this.builder.getMembershipBuilder().createNewInstance().setId(id + 1l).done();
//    user.addToMemberships(membership);
//    assertTrue(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 1);
//    final Set<SMembership> memberships = user.getMemberships();
//    memberships.remove(membership);
//    user.setMemberships(memberships);
//    assertFalse(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 0);
//  }
//
//  @Test
//  public void testSetNullMembershipToUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SMembership membership = this.builder.getMembershipBuilder().createNewInstance().setId(id + 1l).done();
//    user.addToMemberships(membership);
//    assertTrue(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 1);
//    user.setMemberships(null);
//    assertFalse(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 0);
//  }
//
//  @Test
//  public void testSetMembershipsOfUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SMembership membership = this.builder.getMembershipBuilder().createNewInstance().setId(id + 1l).done();
//    final Set<SMembership> memberships = new HashSet<SMembership>();
//    memberships.add(membership);
//    user.setMemberships(memberships);
//    assertTrue(user.hasMembership());
//    assertTrue(user.getMemberships().size() == 1);
//  }
//
//  @Test
//  public void testAddMetadataToUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1l)
//        .done();
//
//    assertFalse(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 0);
//    user.addToMetadata(metadata, "value");
//    assertTrue(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 1);
//  }
//
//  @Test
//  public void testRemoveMetadataFromUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition membership = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1l)
//        .done();
//    user.addToMetadata(membership, "value");
//    assertTrue(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 1);
//    final Map<SProfileMetadataDefinition, String> metadatas = user.getMetadata();
//    metadatas.remove(membership);
//    user.setMetadata(metadatas);
//    assertFalse(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 0);
//  }
//
//  @Test
//  public void testSetNullMetadataToUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition membership = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1l)
//        .done();
//    user.addToMetadata(membership, "value");
//    assertTrue(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 1);
//    user.setMetadata(null);
//    assertFalse(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 0);
//  }
//
//  @Test
//  public void testSetMetadatasOfUser() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1l)
//        .done();
//    final Map<SProfileMetadataDefinition, String> memberships = new HashMap<SProfileMetadataDefinition, String>();
//    memberships.put(metadata, "value");
//    user.setMetadata(memberships);
//    assertTrue(user.hasMetadata());
//    assertTrue(user.getMetadata().size() == 1);
//  }
//
//  @Test
//  public void testRemoveUserFromMetadata() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1l)
//        .done();
//    ((SProfileMetadataDefinitionImpl) metadata).addToUsers(user, "value");
//    assertTrue(metadata.hasUser());
//    assertEquals(1, metadata.getUsers().size());
//    ((SProfileMetadataDefinitionImpl) metadata).removeUser(user);
//    assertFalse(metadata.hasUser());
//    assertEquals(0, metadata.getUsers().size());
//  }
//
//  @Test
//  public void testRemoveUserFromEmptyMetadata() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 1l)
//        .done();
//    ((SProfileMetadataDefinitionImpl) metadata).removeUser(user);
//    assertFalse(metadata.hasUser());
//    assertEquals(0, metadata.getUsers().size());
//  }
//
//  @Test
//  public void testSetUsersToMetadata() {
//    final long id = new Date().getTime();
//    final SUserImpl user1 = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SUserImpl user2 = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id + 1l).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 2l)
//        .done();
//    final Map<SUser, String> map = new HashMap<SUser, String>();
//    map.put(user1, "value1");
//    map.put(user2, "value2");
//    ((SProfileMetadataDefinitionImpl) metadata).setUsers(map);
//    assertTrue(metadata.hasUser());
//    assertEquals(2, metadata.getUsers().size());
//  }
//
//  @Test
//  public void testSetEmptyUsersToMetadata() {
//    final long id = new Date().getTime();
//    final SUserImpl user1 = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    final SUserImpl user2 = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id + 1l).done();
//    final SProfileMetadataDefinition metadata = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id + 2l)
//        .done();
//    final Map<SUser, String> map = new HashMap<SUser, String>();
//    map.put(user1, "value1");
//    map.put(user2, "value2");
//    ((SProfileMetadataDefinitionImpl) metadata).setUsers(map);
//    assertTrue(metadata.hasUser());
//
//    ((SProfileMetadataDefinitionImpl) metadata).setUsers(null);
//    assertFalse(metadata.hasUser());
//  }
//
//  @Test
//  public void testUserDiscriminator() {
//    final long id = new Date().getTime();
//    final SUserImpl user = (SUserImpl) this.builder.getUserBuilder().createNewInstance().setId(id).done();
//    assertEquals(SUser.class.getName(), user.getDiscriminator());
//  }
//
//  @Test
//  public void testRoleDiscriminator() {
//    final long id = new Date().getTime();
//    final SRole role = this.builder.getRoleBuilder().createNewInstance().setId(id).done();
//    assertEquals(SRole.class.getName(), role.getDiscriminator());
//  }
//
//  @Test
//  public void testGroupDiscriminator() {
//    final long id = new Date().getTime();
//    final SGroup group = this.builder.getGroupBuilder().createNewInstance().setId(id).done();
//    assertEquals(SGroup.class.getName(), group.getDiscriminator());
//  }
//
//  @Test
//  public void testMembershipDiscriminator() {
//    final long id = new Date().getTime();
//    final SMembership membership = this.builder.getMembershipBuilder().createNewInstance().setId(id).done();
//    assertEquals(SMembership.class.getName(), membership.getDiscriminator());
//  }
//
//  @Test
//  public void testContactInfoDiscriminator() {
//    final long id = new Date().getTime();
//    final SContactInfo user = this.builder.getContactInfoBuilder().createNewInstance().setId(id).done();
//    assertEquals(SContactInfo.class.getName(), user.getDiscriminator());
//  }
//
//  @Test
//  public void testMetadataDiscriminator() {
//    final long id = new Date().getTime();
//    final SProfileMetadataDefinition user = this.builder.getProfileMetadataDefinitionBuilder().createNewInstance().setId(id).done();
//    assertEquals(SProfileMetadataDefinition.class.getName(), user.getDiscriminator());
//  }

}
