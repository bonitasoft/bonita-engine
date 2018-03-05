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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserImporterTest {

    private static final long USER_ID = 10L;
    private static final long LOCATION_ID = 51L;
    private static final long SKILLS_ID = 50L;
    private static final String lOCATION_VALUE = "engineering";
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
    private TenantServiceAccessor serviceAccessor;
    @Mock
    private CustomUserInfoValueImporter userInfoValueImporter;
    private UserImporter importer;
    @Mock
    private SCustomUserInfoDefinition skills;
    @Mock
    private SCustomUserInfoDefinition location;
    @Mock
    private SUser persistedUser;
    private ExportedUser userToImport;
    private ExportedCustomUserInfoValue skillsValue;
    private ExportedCustomUserInfoValue locationValue;
    @Captor
    private ArgumentCaptor<SUser> captor;
    private SUserImpl manager;
    private SUserImpl currentUser;

    @Before
    public void setUp() throws SUserCreationException {
        given(serviceAccessor.getIdentityService()).willReturn(identityService);
        importer = new UserImporter(serviceAccessor, strategy, 5, userInfoValueImporter);

        given(persistedUser.getId()).willReturn(USER_ID);

        skillsValue = new ExportedCustomUserInfoValue(SKILLS_NAME, SKILLS_VALUE);
        locationValue = new ExportedCustomUserInfoValue(LOCATION_NAME, lOCATION_VALUE);
        manager = new SUserImpl();
        manager.setId(MANAGER_ID);
        manager.setUserName("manager");
        currentUser = new SUserImpl();

        userToImport = getUser(FIRST_USER, Arrays.asList(skillsValue, locationValue));
    }

    private ExportedUser getUser(String username, List<ExportedCustomUserInfoValue> userInfoValues) {
        ExportedUser userImpl = new ExportedUser();
        userImpl.setUserName(username);
        for (ExportedCustomUserInfoValue infoValue : userInfoValues) {
            userImpl.addCustomUserInfoValues(infoValue);
        }
        return userImpl;
    }

    @Test
    public void importUsers_should_call_customUserInfoValueImporter_if_the_user_doesnt_exist() throws Exception {
        // given
        given(identityService.getNumberOfUsers(any(QueryOptions.class))).willReturn(0L);
        given(identityService.createUser(any(SUser.class))).willReturn(persistedUser);

        // when
        importer.importUsers(Arrays.asList(userToImport));

        // then
        verify(userInfoValueImporter, times(1)).imporCustomUserInfoValues(Arrays.asList(skillsValue, locationValue), USER_ID);
    }

    @Test
    public void importUsers_shouldnt_call_customUserInfoValueImporter_if_the_user_exists() throws Exception {
        // given
        given(identityService.getNumberOfUsers(any(QueryOptions.class))).willReturn(1L);
        given(identityService.getUserByUserName(FIRST_USER)).willReturn(persistedUser);

        // when
        importer.importUsers(Collections.singletonList(userToImport));

        // then
        verify(userInfoValueImporter, never()).imporCustomUserInfoValues(Matchers.<List<ExportedCustomUserInfoValue>>any(), anyLong());
    }
}
