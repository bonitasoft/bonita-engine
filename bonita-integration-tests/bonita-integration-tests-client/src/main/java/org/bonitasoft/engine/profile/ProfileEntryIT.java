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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileEntryIT extends AbstractProfileIT {

    @Test
    public void searchProfileEntry() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        final SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT, searchedProfileEntries.getCount());
        final List<ProfileEntry> result = searchedProfileEntries.getResult();
        assertEquals(10, result.size());
    }

    @Test
    public void should_getProfileEntries_of_a_profile() throws ProfileNotFoundException {
        List<ProfileEntry> profileEntries = getProfileAPI().getProfileEntries("Administrator");

        assertThat(profileEntries).hasSize(ADMIN_PROFILE_ENTRY_COUNT);
    }

    @Test(expected = SearchException.class)
    public void searchProfileEntryWithWrongParameter() throws Exception {
        getProfileAPI().searchProfileEntries(null);
    }

    @Test(expected = ProfileEntryNotFoundException.class)
    public void getProfileEntryWithWrongParameter() throws Exception {
        getProfileAPI().getProfileEntry(9645L);
    }

}
