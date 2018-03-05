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
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ImportOrganizationMergeDuplicatesStrategyTest {

    private static final long USER_ID = 2L;
    private static final long DEFINITION_ID = 1L;
    private static final String SKILLS_VALUE = "Java";
    private static final String SKILLS_NAME = "Skills";
    private static final long MANAGER_ID = 1954238L;
    @Mock
    private IdentityService identityService;
    @Mock
    private SCustomUserInfoValueAPI userInfoValueAPI;
    @InjectMocks
    private ImportOrganizationMergeDuplicatesStrategy strategy;
    @Mock
    private SUser existingUser;
    @Mock
    private TechnicalLoggerService logger;
    private ExportedUser userToImport;
    @Mock
    private SCustomUserInfoDefinition infoDef;
    @Captor
    private ArgumentCaptor<EntityUpdateDescriptor> captor;
    private SUserImpl manager;

    @Before
    public void setUp() {
        given(existingUser.getId()).willReturn(USER_ID);
        given(infoDef.getId()).willReturn(DEFINITION_ID);
        manager = new SUserImpl();
        manager.setId(MANAGER_ID);
        manager.setUserName("manager");
        userToImport = new ExportedUser();
    }

    @Test
    public void foundExistingCustomUserInfoDefinition_should_call_service_to_update_element() throws Exception {
        // given
        String newDescription = "updated description";
        SCustomUserInfoDefinition existingUserInfoDefinition = mock(SCustomUserInfoDefinition.class);
        ExportedCustomUserInfoDefinition creator = new ExportedCustomUserInfoDefinition("name", newDescription);
        EntityUpdateDescriptor updateDescriptor = getUpdateDescriptor(newDescription);
        userToImport.addCustomUserInfoValues(new ExportedCustomUserInfoValue(SKILLS_NAME, SKILLS_VALUE));

        // when
        strategy.foundExistingCustomUserInfoDefinition(existingUserInfoDefinition, creator);

        // then
        verify(identityService, times(1)).updateCustomUserInfoDefinition(existingUserInfoDefinition, updateDescriptor);
    }

    private EntityUpdateDescriptor getUpdateDescriptor(String newDescription) {
        SCustomUserInfoDefinitionUpdateBuilder builder = BuilderFactory.get(SCustomUserInfoDefinitionUpdateBuilderFactory.class).createNewInstance();
        builder.updateDescription(newDescription);
        return builder.done();
    }

    @Test
    public void foundExistingUser_show_call_customUserInfoValueImporter() throws Exception {
        // given
        given(identityService.getCustomUserInfoDefinitionByName(SKILLS_NAME)).willReturn(infoDef);
        userToImport.addCustomUserInfoValues(new ExportedCustomUserInfoValue(SKILLS_NAME, SKILLS_VALUE));

        // when
        strategy.foundExistingUser(existingUser, userToImport);

        // then
        verify(userInfoValueAPI, times(1)).set(DEFINITION_ID, USER_ID, SKILLS_VALUE);
    }


}
