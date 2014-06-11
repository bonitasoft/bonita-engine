package org.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;

import java.util.List;

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
public class ProfileEntryITest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Search" }, story = "Search profile entry.", jira = "")
    @Test
    public void searchProfileEntry() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        final SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(24, searchedProfileEntries.getCount());
        final List<ProfileEntry> result = searchedProfileEntries.getResult();
        assertEquals(10, result.size());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = SearchException.class)
    public void searchProfileEntryWithWrongParameter() throws Exception {
        getProfileAPI().searchProfileEntries(null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = ProfileEntryNotFoundException.class)
    public void getProfileEntryWithWrongParameter() throws Exception {
        getProfileAPI().getProfileEntry(9645L);
    }

}
