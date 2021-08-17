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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.profile.xml.MembershipNode;
import org.bonitasoft.engine.profile.xml.ProfileNode;
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
        String allProfilesXml = new String(
                IOUtils.toByteArray(this.getClass().getResourceAsStream("/AllProfiles.xml")));
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

}
