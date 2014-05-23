/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.junit.After;
import org.junit.Before;

import com.bonitasoft.engine.CommonAPISPTest;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractProfileTest extends CommonAPISPTest {

    protected static final String IMPORT_PROFILES_CMD = "importProfilesCommand";

    protected static final String PROFILE_MEMBER_ID = "profileMemberId";

    protected static final int ADMIN_PROFILE_ENTRY_COUNT = 24;

    protected static final int USER_PROFILE_ENTRY_COUNT = 17;

    protected long adminProfileId;

    protected long userProfileId;

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
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();

        createUsers();

        group1 = createGroup("group1");
        group2 = createGroup("group2");
        group3 = createGroup("group3");
        role1 = createRole("role1");
        role2 = createRole("role2");
        role3 = createRole("role3");

        // Restoring up default profiles before tests:
        final InputStream xmlStream = AbstractProfileTest.class.getResourceAsStream("AllProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
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

    private List<User> createUsers() throws BonitaException {
        user1 = createUser("userName1", "User1Pwd", "User1FirstName", "User1LastName");
        user2 = createUser("userName2", "User2Pwd", "User2FirstName", "User2LastName");
        user3 = createUser("userName3", "User3Pwd", "User3FirstName", "User3LastName");
        user4 = createUser("userName4", "User4Pwd", "User4FirstName", "User4LastName");
        user5 = createUser("userName5", "User5Pwd", "User5FirstName", "User5LastName");
        return Arrays.asList(user1, user2, user3, user4, user5);
    }

    @After
    public void afterTest() throws BonitaException, IOException {
        deleteUsers(user1, user2, user3, user4, user5);
        deleteGroups(group1, group2, group3);
        deleteRoles(role1, role2, role3);

        // Clean profiles
        final InputStream xmlStream = AbstractProfileTest.class.getResourceAsStream("CleanProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        final Map<String, Serializable> importParameters = new HashMap<String, Serializable>(1);
        importParameters.put("xmlContent", xmlContent);
        getCommandAPI().execute(IMPORT_PROFILES_CMD, importParameters);

       logoutOnTenant();
    }

}
