/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestWithApplication extends CommonAPIIT {

    private User user;

    @Before
    public void setUp() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser("john", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(new SearchOptionsBuilder(0, 1000).done());
        for (final Application app : searchResult.getResult()) {
            getApplicationAPI().deleteApplication(app.getId());
        }
        logoutThenlogin();
        deleteUser(user);
        logoutOnTenant();
    }

    protected Profile getProfileUser() throws SearchException {
        return getProfile("User");
    }

    protected Profile getProfileAdmin() throws SearchException {
        return getProfile("Administrator");
    }

    protected Profile getProfile(String profileName) throws SearchException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
        builder.filter(ProfileSearchDescriptor.NAME, profileName);
        SearchResult<Profile> profileSearchResult = getProfileAPI().searchProfiles(builder.done());
        assertThat(profileSearchResult.getCount()).isEqualTo(1);
        Profile profile = profileSearchResult.getResult().get(0);
        return profile;
    }

    public User getUser() {
        return user;
    }
}
