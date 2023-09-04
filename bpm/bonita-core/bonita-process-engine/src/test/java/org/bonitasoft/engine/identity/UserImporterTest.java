/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserImporterTest {

    private static final long USER_ID = 10L;
    private static final String LOCATION_VALUE = "engineering";
    private static final String LOCATION_NAME = "location";
    private static final String SKILLS_VALUE = "Java";
    private static final String FIRST_USER = "first.user";
    private static final String SKILLS_NAME = "Skills";
    private static final long MANAGER_ID = 1954238L;
    @Mock
    private IdentityService identityService;
    @Mock
    private ImportOrganizationStrategy strategy;
    @Mock
    private CustomUserInfoValueImporter userInfoValueImporter;
    private UserImporter importer;
    @Mock
    private SUser persistedUser;
    private ExportedUser userToImport;
    private ExportedCustomUserInfoValue skillsValue;
    private ExportedCustomUserInfoValue locationValue;

    public UserImporterTest() {
    }

    @Before
    public void setUp() throws SUserCreationException {
        importer = new UserImporter(identityService, strategy, 5, userInfoValueImporter);

        given(persistedUser.getId()).willReturn(USER_ID);

        skillsValue = new ExportedCustomUserInfoValue(SKILLS_NAME, SKILLS_VALUE);
        locationValue = new ExportedCustomUserInfoValue(LOCATION_NAME, LOCATION_VALUE);
        SUser manager = new SUser();
        manager.setId(MANAGER_ID);
        manager.setUserName("manager");

        userToImport = getUser(Arrays.asList(skillsValue, locationValue));
    }

    private ExportedUser getUser(List<ExportedCustomUserInfoValue> userInfoValues) {
        ExportedUser userImpl = new ExportedUser();
        userImpl.setUserName(UserImporterTest.FIRST_USER);
        for (ExportedCustomUserInfoValue infoValue : userInfoValues) {
            userImpl.addCustomUserInfoValues(infoValue);
        }
        return userImpl;
    }

    @Test
    public void importUsers_should_call_customUserInfoValueImporter_if_the_user_does_not_exist() throws Exception {
        // given
        given(identityService.getNumberOfUsers(any(QueryOptions.class))).willReturn(0L);
        given(identityService.createUser(any(SUser.class))).willReturn(persistedUser);

        // when
        importer.importUsers(singletonList(userToImport));

        // then
        verify(userInfoValueImporter, times(1)).imporCustomUserInfoValues(Arrays.asList(skillsValue, locationValue),
                USER_ID);
    }

    @Test
    public void importUsers_should_not_call_customUserInfoValueImporter_if_the_user_exists() throws Exception {
        // given
        given(identityService.getNumberOfUsers(any(QueryOptions.class))).willReturn(1L);
        given(identityService.getUserByUserName(FIRST_USER)).willReturn(persistedUser);

        // when
        importer.importUsers(singletonList(userToImport));

        // then
        verify(userInfoValueImporter, never())
                .imporCustomUserInfoValues(ArgumentMatchers.any(), anyLong());
    }
}
