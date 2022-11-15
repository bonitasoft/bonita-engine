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
package org.bonitasoft.web.rest.server.datastore.profile.member;

import static junit.framework.Assert.assertTrue;
import static org.bonitasoft.web.rest.model.builder.profile.member.EngineProfileMemberBuilder.anEngineProfileMember;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.portal.profile.ProfileMemberItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.utils.SearchUtils;
import org.bonitasoft.web.rest.server.engineclient.ProfileMemberEngineClient;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Vincent Elcrin
 */
public class SearchProfileMembersHelperTest extends APITestWithMock {

    @Mock
    ProfileMemberEngineClient engineClient;

    SearchProfileMembersHelper searchProfilesHelper;

    @Before
    public void setUp() {
        initMocks(this);
        searchProfilesHelper = new SearchProfileMembersHelper(engineClient);
    }

    @Test
    public void testWeCanSearchProfileMembers() throws Exception {
        SearchResultImpl<ProfileMember> aKnownSearchResult = aKnownSearchResult();
        List<ProfileMemberItem> expectedProfileMemberItems = new ProfileMemberItemConverter()
                .convert(aKnownSearchResult().getResult());
        when(engineClient.searchProfileMembers(eq(MemberType.ROLE.getType()), any(SearchOptions.class)))
                .thenReturn(aKnownSearchResult);
        HashMap<String, String> filters = filterOnProfileIdAndMemberType(5L, MemberType.ROLE);

        ItemSearchResult<ProfileMemberItem> searchResult = searchProfilesHelper.search(0, 10, null, null, filters);

        assertTrue(SearchUtils.areEquals(expectedProfileMemberItems, searchResult.getResults()));
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void testSearchWithoutMandatoryFiltersThrowError() {

        searchProfilesHelper.search(0, 10, null, null, Collections.<String, String> emptyMap());
    }

    private SearchResultImpl<ProfileMember> aKnownSearchResult() {
        return SearchUtils.createEngineSearchResult(aKnownProfile(), anotherKnownProfile());

    }

    private HashMap<String, String> filterOnProfileIdAndMemberType(long id, MemberType type) {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(ProfileMemberItem.ATTRIBUTE_PROFILE_ID, String.valueOf(id));
        filters.put(ProfileMemberItem.FILTER_MEMBER_TYPE, type.getType());
        return filters;
    }

    private ProfileMember aKnownProfile() {
        return anEngineProfileMember().build();
    }

    private ProfileMember anotherKnownProfile() {
        return anEngineProfileMember()
                .withId(2L)
                .withProfileId(3L)
                .withUserId(4L)
                .withGroupId(5L)
                .withRoleId(6l)
                .build();
    }

}
