/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.datastore.profile.EngineProfileBuilder.anEngineProfile;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.portal.profile.ProfileItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.utils.SearchUtils;
import org.bonitasoft.web.rest.server.engineclient.ProfileEngineClient;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Vincent Elcrin
 */
public class SearchProfilesHelperTest extends APITestWithMock {

    @Mock
    ProfileAPI profileAPI;

    SearchProfilesHelper searchProfilesHelper;

    @Before
    public void setUp() {
        initMocks(this);
        searchProfilesHelper = new SearchProfilesHelper(new ProfileEngineClient(profileAPI));
    }

    @Test
    public void testWeCanSearchProfiles() throws Exception {
        final SearchResultImpl<Profile> aKnownSearchResult = aKnownSearchResult();
        final List<ProfileItem> expectedProfiles = new ProfileItemConverter().convert(aKnownSearchResult.getResult());
        when(profileAPI.searchProfiles(any(SearchOptions.class))).thenReturn(aKnownSearchResult);

        final ItemSearchResult<ProfileItem> searchResult = searchProfilesHelper.search(0, 10, null, null, null);

        assertThat(SearchUtils.areEquals(expectedProfiles, searchResult.getResults())).isTrue();
    }

    @Test
    public void testWeCanListUserProfiles() {
        final SearchResultImpl<Profile> aKnownSearchResult = aKnownSearchResult();
        final List<ProfileItem> expectedProfiles = new ProfileItemConverter().convert(aKnownSearchResult.getResult());
        when(profileAPI.getProfilesForUser(2L, 0, Integer.MAX_VALUE, ProfileCriterion.ID_ASC))
                .thenReturn(aKnownSearchResult.getResult());

        final ItemSearchResult<ProfileItem> searchResult = searchProfilesHelper.search(0, 10, null, null,
                filterOnUserId(2L));

        verify(profileAPI).getProfilesForUser(2L, 0, Integer.MAX_VALUE, ProfileCriterion.ID_ASC);
        assertThat(SearchUtils.areEquals(expectedProfiles, searchResult.getResults())).isTrue();
    }

    private HashMap<String, String> filterOnUserId(final long id) {
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ProfileItem.FILTER_USER_ID, String.valueOf(id));
        return filters;
    }

    private SearchResultImpl<Profile> aKnownSearchResult() {
        final Profile aKnownProfile = anEngineProfile().withName("aName").withDescription("aDescription").build();
        final Profile anotherKnownProfile = anEngineProfile().withName("anotherName")
                .withDescription("anotherDescription").build();
        return SearchUtils.createEngineSearchResult(aKnownProfile, anotherKnownProfile);
    }
}
