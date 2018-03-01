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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileEntryImpl;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;
import org.bonitasoft.engine.profile.xml.ParentProfileEntryNode;
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
        SProfileImpl profile1 = new SProfileImpl();
        profile1.setName("MyProfile1");
        SProfileImpl profile2 = new SProfileImpl();
        profile2.setName("MyProfile2");
        //when
        ProfilesNode exportedProfiles = profilesExporter.toProfiles(Arrays.asList((SProfile) profile1, profile2));
        //then
        assertThat(exportedProfiles.getProfiles()).containsOnly(new ProfileNode("MyProfile1", false), new ProfileNode("MyProfile2", false));
    }

    @Test
    public void should_convert_profile_having_profile_entry_to_exported_version() throws Exception {
        //given
        SProfileImpl profile1 = new SProfileImpl();
        profile1.setName("MyProfile1");
        profile1.setId(12L);
        SProfileEntryImpl sProfileEntry1 = new SProfileEntryImpl();
        sProfileEntry1.setName("p1");
        SProfileEntryImpl sProfileEntry2 = new SProfileEntryImpl();
        sProfileEntry2.setName("p2");
        doReturn(Arrays.asList(sProfileEntry1, sProfileEntry2)).doReturn(Collections.emptyList()).when(profileService)
                .searchProfileEntries(any(QueryOptions.class));
        //when
        ProfilesNode exportedProfiles = profilesExporter.toProfiles(Collections.singletonList((SProfile) profile1));
        //then
        ProfileNode myProfile1 = new ProfileNode("MyProfile1", false);
        myProfile1.setParentProfileEntries(Arrays.asList(new ParentProfileEntryNode("p1"), new ParentProfileEntryNode("p2")));
        assertThat(exportedProfiles.getProfiles()).containsOnly(myProfile1);
    }

}
