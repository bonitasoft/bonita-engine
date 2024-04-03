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

import java.util.Arrays;

import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.xml.ProfileNode;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfilesExporterTest {

    @Mock
    private IdentityService identityService;
    @Mock
    private ProfileService profileService;
    @Mock
    private ProfilesParser profilesParser;
    @InjectMocks
    private ProfilesExporter profilesExporter;

    @Test
    public void should_convert_profiles_to_exported_version() throws Exception {
        //given
        SProfile profile1 = SProfile.builder().build();
        profile1.setName("MyProfile1");
        SProfile profile2 = SProfile.builder().build();
        profile2.setName("MyProfile2");
        //when
        ProfilesNode exportedProfiles = profilesExporter.toProfiles(Arrays.asList(profile1, profile2));
        //then
        assertThat(exportedProfiles.getProfiles()).containsOnly(new ProfileNode("MyProfile1", false),
                new ProfileNode("MyProfile2", false));
    }

}
