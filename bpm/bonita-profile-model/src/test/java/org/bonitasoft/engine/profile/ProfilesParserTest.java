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
package org.bonitasoft.engine.profile;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.profile.xml.MembershipNode;
import org.bonitasoft.engine.profile.xml.ProfileMappingNode;
import org.bonitasoft.engine.profile.xml.ProfileNode;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ProfilesParserTest {

    private final ProfilesParser profilesParser = new ProfilesParser();

    @Test
    public void should_be_able_to_parse_AllProfilesXml_file() throws Exception {
        //given
        String allProfilesXml;
        try (var inputStream = this.getClass().getResourceAsStream("/AllProfiles.xml")) {
            assertThat(inputStream).isNotNull();
            allProfilesXml = new String(inputStream.readAllBytes());
        }
        //when
        ProfilesNode exportedProfiles = profilesParser.convert(allProfilesXml);

        //then
        assertThat(exportedProfiles.getProfiles()).hasSize(4);
        ProfileNode adminProfile = exportedProfiles.getProfiles().get(0);
        assertThat(adminProfile.getName()).isEqualTo("Administrator");
        assertThat(adminProfile.isDefault()).isTrue();
        assertThat(adminProfile.getDescription()).isEqualTo("Administrator profile");
        assertThat(adminProfile.getProfileMapping().getUsers()).containsOnly("userName1");
        assertThat(adminProfile.getProfileMapping().getRoles()).containsOnly("role1", "role2");
        assertThat(adminProfile.getProfileMapping().getGroups()).isEmpty();
        assertThat(adminProfile.getProfileMapping().getMemberships()).containsOnly(
                new MembershipNode("/group1", "role2"),
                new MembershipNode("/group2", "role2"));
    }

    @Test
    public void should_export_profile_to_xml() throws Exception {
        ProfileNode myCustomProfile = new ProfileNode("myCustomProfile", false);
        ProfilesNode model = new ProfilesNode(singletonList(
                myCustomProfile));
        myCustomProfile.setDescription("This is my custom profile");
        ProfileMappingNode profileMapping = new ProfileMappingNode();
        myCustomProfile.setProfileMapping(profileMapping);
        profileMapping.setGroups(asList("g1", "g2", "g3"));
        profileMapping.setRoles(asList("r1", "r2", "r3"));
        profileMapping.setUsers(asList("u1", "u2", "u3"));
        profileMapping.setMemberships(asList(
                new MembershipNode("g4", "r4"),
                new MembershipNode("g5", "r5")));

        String convert = profilesParser.convert(model);

        // xml exported
        XMLAssert.assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<profiles:profiles xmlns:profiles=\"http://documentation.bonitasoft.com/profile-xml-schema/1.0\">\n" +
                "    <profile name=\"myCustomProfile\" isDefault=\"false\">\n" +
                "        <description>This is my custom profile</description>\n" +
                "        <profileMapping>\n" +
                "            <users>\n" +
                "                <user>u1</user>\n" +
                "                <user>u2</user>\n" +
                "                <user>u3</user>\n" +
                "            </users>\n" +
                "            <groups>\n" +
                "                <group>g1</group>\n" +
                "                <group>g2</group>\n" +
                "                <group>g3</group>\n" +
                "            </groups>\n" +
                "            <memberships>\n" +
                "                <membership>\n" +
                "                    <group>g4</group>\n" +
                "                    <role>r4</role>\n" +
                "                </membership>\n" +
                "                <membership>\n" +
                "                    <group>g5</group>\n" +
                "                    <role>r5</role>\n" +
                "                </membership>\n" +
                "            </memberships>\n" +
                "            <roles>\n" +
                "                <role>r1</role>\n" +
                "                <role>r2</role>\n" +
                "                <role>r3</role>\n" +
                "            </roles>\n" +
                "        </profileMapping>\n" +
                "    </profile>\n" +
                "</profiles:profiles>", convert);
    }
}
