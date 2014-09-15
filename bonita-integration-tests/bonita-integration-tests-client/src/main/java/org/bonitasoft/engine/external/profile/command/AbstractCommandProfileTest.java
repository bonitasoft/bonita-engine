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
package org.bonitasoft.engine.external.profile.command;

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
import org.bonitasoft.engine.profile.AbstractProfileTest;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractCommandProfileTest extends CommonAPITest {

    protected static final String IMPORT_PROFILES_CMD = "importProfilesCommand";

    protected static final long ADMIN_PROFILE_ENTRY_COUNT = 24;

    protected static final long USER_PROFILE_ENTRY_COUNT = 17;

    protected Long adminProfileId;

    protected Long userProfileId;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        // Restoring up default profiles before tests:
        final InputStream xmlStream = ProfileImportCommandTest.class.getResourceAsStream("RestoreDefaultProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        xmlStream.close();
        final Map<String, Serializable> importParameters = new HashMap<String, Serializable>();
        importParameters.put("xmlContent", xmlContent);
        getCommandAPI().execute(IMPORT_PROFILES_CMD, importParameters);

        // search for the newly created profile IDs:
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 50).sort(ProfileSearchDescriptor.ID, Order.ASC).done();
        final SearchResult<Profile> searchProfiles = getProfileAPI().searchProfiles(searchOptions);
        final List<Profile> profiles = searchProfiles.getResult();
        assertEquals(2, profiles.size());
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
