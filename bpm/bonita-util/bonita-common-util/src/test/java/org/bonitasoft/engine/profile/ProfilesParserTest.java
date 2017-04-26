/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.profile.xml.MembershipNode;
import org.bonitasoft.engine.profile.xml.ParentProfileEntryNode;
import org.bonitasoft.engine.profile.xml.ProfileNode;
import org.bonitasoft.engine.profile.xml.ProfileEntryNode;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ProfilesParserTest {

    private ProfilesParser profilesParser = new ProfilesParser();

    @Test
    public void should_be_able_to_parse_AllProfilesXml_file() throws Exception {
        //given
        String allProfilesXml = new String(IOUtils.toByteArray(this.getClass().getResourceAsStream("/AllProfiles.xml")));
        //when
        ProfilesNode exportedProfiles = profilesParser.convert(allProfilesXml);

        //then
        assertThat(exportedProfiles.getProfiles()).hasSize(4);
        ProfileNode adminProfile = exportedProfiles.getProfiles().get(0);
        assertThat(adminProfile.getName()).isEqualTo("Administrator");
        assertThat(adminProfile.isDefault()).isTrue();
        assertThat(adminProfile.getDescription()).isEqualTo("Administrator profile");
        assertThat(adminProfile.getParentProfileEntries()).hasSize(7);
        ParentProfileEntryNode organizationFolder = adminProfile.getParentProfileEntries().get(1);
        assertThat(organizationFolder.getName()).isEqualTo("Organization");
        assertThat(organizationFolder.isCustom()).isFalse();
        assertThat(organizationFolder.getIndex()).isEqualTo(2);
        assertThat(organizationFolder.getDescription()).isEqualTo("Organization");
        assertThat(organizationFolder.getType()).isEqualTo("folder");
        assertThat(organizationFolder.getPage()).isEqualTo("1");
        List<ProfileEntryNode> organizationChildren = organizationFolder.getChildProfileEntries();
        assertThat(organizationChildren).hasSize(4);
        ProfileEntryNode rolesProfileEntry = organizationChildren.get(2);
        assertThat(rolesProfileEntry.getName()).isEqualTo("Roles");
        assertThat(rolesProfileEntry.isCustom()).isFalse();
        assertThat(rolesProfileEntry.getIndex()).isEqualTo(4);
        assertThat(rolesProfileEntry.getType()).isEqualTo("link");
        assertThat(rolesProfileEntry.getPage()).isEqualTo("AllRoles");
        assertThat(adminProfile.getProfileMapping().getUsers()).containsOnly("userName1");
        assertThat(adminProfile.getProfileMapping().getRoles()).containsOnly("role1", "role2");
        assertThat(adminProfile.getProfileMapping().getGroups()).isEmpty();
        assertThat(adminProfile.getProfileMapping().getMemberships()).containsOnly(new MembershipNode("/group1", "role2"),
                new MembershipNode("/group2", "role2"));
    }

    @Test
    public void should_import_profile_xml_containing_obsolete_parentName() throws Exception {
        //given
        final String xmlAsText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<profiles:profiles xmlns:profiles=\"http://www.bonitasoft.org/ns/profile/6.1\">"
                + "<profile isDefault=\"false\" name=\"ProfileWithNull\"> "
                + "<description>ImportExportProfileDescription</description>"
                + "<profileEntries>"
                + "<parentProfileEntry isCustom=\"true\" name=\"menu3\">"
                + "<parentName>NULL</parentName>"
                + "<index>0</index>"
                + "<type>folder</type>"
                + "</parentProfileEntry>"
                + "</profileEntries>"
                + "</profile>"
                + "</profiles:profiles>";

        //when
        ProfilesNode exportedProfiles = profilesParser.convert(xmlAsText);
        //then
        ParentProfileEntryNode menu3 = new ParentProfileEntryNode("menu3");
        menu3.setCustom(true);
        menu3.setType("folder");
        assertThat(exportedProfiles.getProfiles().get(0).getParentProfileEntries().get(0)).isEqualTo(menu3);
    }

}
