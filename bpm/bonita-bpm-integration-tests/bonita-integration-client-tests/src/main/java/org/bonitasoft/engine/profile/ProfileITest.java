/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileITest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Search" }, story = "Search profile.", jira = "")
    @Test
    public void searchProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);

        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        assertEquals(4, searchedProfiles.getCount());
        assertEquals("User", searchedProfiles.getResult().get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Search" }, story = "Search profile.", jira = "")
    @Test
    public void searchProfileWithSearchTerm() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("Adm");

        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        assertEquals(1, searchedProfiles.getCount());
        assertEquals("Administrator", searchedProfiles.getResult().get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Search", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = SearchException.class)
    public void searchProfileWithWrongParameter() throws Exception {
        getProfileAPI().searchProfiles(null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = ProfileNotFoundException.class)
    public void getProfileWithWrongParameter() throws Exception {
        getProfileAPI().getProfile(855);
    }

}
