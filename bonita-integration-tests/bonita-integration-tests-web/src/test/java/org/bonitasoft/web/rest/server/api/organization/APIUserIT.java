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
package org.bonitasoft.web.rest.server.api.organization;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author Colin PUY
 */
@SuppressWarnings("unchecked")
public class APIUserIT extends AbstractConsoleTest {

    private static final String ASCENDING = " asc";
    private static final String DESCENDING = " desc";

    private APIUser apiUser;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiUser = new APIUser();
        apiUser.setCaller(getAPICaller(getInitiator().getSession(), "API/identity/user"));

    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private boolean lowerThan(final String string1, final String string2) {
        return string1.compareTo(string2) < 0;
    }

    private boolean upperThan(final String string1, final String string2) {
        return string1.compareTo(string2) > 0;
    }

    @Test
    public void searchCanBeOrderdByFirstNameAscending() throws Exception {
        TestUserFactory.getRidleyScott();
        TestUserFactory.getJohnCarpenter();

        final ItemSearchResult<UserItem> searchResult = apiUser.runSearch(0, 10, null,
                UserItem.ATTRIBUTE_FIRSTNAME + ASCENDING,
                EMPTY_MAP, EMPTY_LIST, EMPTY_LIST);

        final String firstUserFirstName = searchResult.getResults().get(0).getFirstName();
        final String secondUserFirstName = searchResult.getResults().get(1).getFirstName();
        assertTrue(lowerThan(firstUserFirstName, secondUserFirstName));
    }

    @Test
    public void searchCanBeOrderdByFirstNameDescending() throws Exception {
        TestUserFactory.getRidleyScott();
        TestUserFactory.getJohnCarpenter();

        final ItemSearchResult<UserItem> searchResult = apiUser.runSearch(0, 10, null,
                UserItem.ATTRIBUTE_FIRSTNAME + DESCENDING,
                EMPTY_MAP, EMPTY_LIST, EMPTY_LIST);

        final String firstUserFirstName = searchResult.getResults().get(0).getFirstName();
        final String secondUserFirstName = searchResult.getResults().get(1).getFirstName();
        assertTrue(upperThan(firstUserFirstName, secondUserFirstName));
    }

    @Test
    public void searchCanBeOrderdByLastNameAscending() throws Exception {
        TestUserFactory.getRidleyScott();
        TestUserFactory.getJohnCarpenter();

        final ItemSearchResult<UserItem> searchResult = apiUser.runSearch(0, 10, null,
                UserItem.ATTRIBUTE_LASTNAME + ASCENDING,
                EMPTY_MAP, EMPTY_LIST, EMPTY_LIST);

        final String firstUserLastName = searchResult.getResults().get(0).getLastName();
        final String secondUserLastName = searchResult.getResults().get(1).getLastName();
        assertTrue(lowerThan(firstUserLastName, secondUserLastName));
    }

    @Test
    public void searchCanBeOrderdByLastNameDescending() throws Exception {
        TestUserFactory.getRidleyScott();
        TestUserFactory.getJohnCarpenter();

        final ItemSearchResult<UserItem> searchResult = apiUser.runSearch(0, 10, null,
                UserItem.ATTRIBUTE_LASTNAME + DESCENDING,
                EMPTY_MAP, EMPTY_LIST, EMPTY_LIST);

        final String firstUserLastName = searchResult.getResults().get(0).getLastName();
        final String secondUserLastName = searchResult.getResults().get(1).getLastName();
        assertTrue(upperThan(firstUserLastName, secondUserLastName));
    }

    @Test
    public void searchCanBeOrderdByUserNameAscending() throws Exception {
        TestUserFactory.getRidleyScott();
        TestUserFactory.getJohnCarpenter();

        final ItemSearchResult<UserItem> searchResult = apiUser.runSearch(0, 10, null,
                UserItem.ATTRIBUTE_USERNAME + ASCENDING,
                EMPTY_MAP, EMPTY_LIST, EMPTY_LIST);

        final String firstUserUserName = searchResult.getResults().get(0).getUserName();
        final String secondUserUserName = searchResult.getResults().get(1).getUserName();
        assertTrue(lowerThan(firstUserUserName, secondUserUserName));
    }

    @Test
    public void searchCanBeOrderdByUserNameDescending() throws Exception {
        TestUserFactory.getRidleyScott();
        TestUserFactory.getJohnCarpenter();

        final ItemSearchResult<UserItem> searchResult = apiUser.runSearch(0, 10, null,
                UserItem.ATTRIBUTE_USERNAME + DESCENDING,
                EMPTY_MAP, EMPTY_LIST, EMPTY_LIST);

        final String firstUserUserName = searchResult.getResults().get(0).getUserName();
        final String secondUserUserName = searchResult.getResults().get(1).getUserName();
        assertTrue(upperThan(firstUserUserName, secondUserUserName));
    }
}
