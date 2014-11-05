/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.junit.After;
import org.junit.Before;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractProfileTest extends CommonAPITest {

    protected static final String IMPORT_PROFILES_CMD = "importProfilesCommand";

    protected static final int ADMIN_PROFILE_ENTRY_COUNT = 24;

    protected static final int USER_PROFILE_ENTRY_COUNT = 17;

    protected Long adminProfileId;

    protected Long userProfileId;

    protected User user1;

    protected User user2;

    protected User user3;

    protected User user4;

    protected User user5;

    protected Group group1;

    protected Group group2;

    protected Group group3;

    protected Role role1;

    protected Role role2;

    protected Role role3;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        user1 = createUser("userName1", "User1Pwd", "User1FirstName", "User1LastName");
        user2 = createUser("userName2", "User2Pwd", "User2FirstName", "User2LastName");
        user3 = createUser("userName3", "User3Pwd", "User3FirstName", "User3LastName");
        user4 = createUser("userName4", "User4Pwd", "User4FirstName", "User4LastName");
        user5 = createUser("userName5", "User5Pwd", "User5FirstName", "User5LastName");

        group1 = createGroup("group1");
        group2 = createGroup("group2");
        group3 = createGroup("group3");
        role1 = createRole("role1");
        role2 = createRole("role2");
        role3 = createRole("role3");

        // Restoring up default profiles before tests:
        // Restoring up default profiles before tests:
        final InputStream xmlStream = AbstractProfileTest.class.getResourceAsStream("AllProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        xmlStream.close();
        final Map<String, Serializable> importParameters = new HashMap<String, Serializable>(1);
        importParameters.put("xmlContent", xmlContent);
        getCommandAPI().execute(IMPORT_PROFILES_CMD, importParameters);

        // search for the newly created profile IDs:
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, Integer.MAX_VALUE).sort(ProfileSearchDescriptor.NAME, Order.ASC);
        final List<Profile> profiles = getProfileAPI().searchProfiles(builder.done()).getResult();
        assertEquals(4, profiles.size());
        for (final Profile map : profiles) {
            if ("Administrator".equals(map.getName())) {
                adminProfileId = map.getId();
            } else if ("User".equals(map.getName())) {
                userProfileId = map.getId();
            }
        }
    }

    @After
    public void after() throws BonitaException, IOException {
        deleteUser(user1.getId());
        deleteUser(user2.getId());
        deleteUser(user3.getId());
        deleteUser(user4.getId());
        deleteUser(user5.getId());

        deleteGroups(group1, group2, group3);
        deleteRoles(role1, role2, role3);

        // Clean profiles
        final InputStream xmlStream = AbstractProfileTest.class.getResourceAsStream("CleanProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        xmlStream.close();
        final Map<String, Serializable> importParameters = new HashMap<String, Serializable>(1);
        importParameters.put("xmlContent", xmlContent);
        getCommandAPI().execute(IMPORT_PROFILES_CMD, importParameters);

        logoutOnTenant();
    }

}
