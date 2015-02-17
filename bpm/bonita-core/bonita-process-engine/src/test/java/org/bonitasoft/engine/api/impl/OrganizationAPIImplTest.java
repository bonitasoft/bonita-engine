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
package org.bonitasoft.engine.api.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationAPIImplTest {

    private static final int PAGE_SIZE = 1;

    private static final long CUSTOM_USER_INFO_DEF_ID1 = 11;

    private static final long CUSTOM_USER_INFO_DEF_ID2 = 12;

    @Mock
    private TenantServiceAccessor serviceAccessor;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private SCommentService commentService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private IdentityService identityService;

    @Mock
    private ActorMappingService actorMappingService;

    @Mock
    private ProfileService profileService;

    @Mock
    private SupervisorMappingService supervisorService;

    @Mock
    private ExternalIdentityMappingService externalIdentityMappingService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private DependencyResolver dependencyResolver;

    @Mock
    private SCustomUserInfoDefinition userInfoDef1;

    @Mock
    private SCustomUserInfoDefinition userInfoDef2;

    private OrganizationAPIImpl organizationAPIImpl;

    @Before
    public void setUp() {
        organizationAPIImpl = new OrganizationAPIImpl(serviceAccessor, PAGE_SIZE);

        given(serviceAccessor.getProcessInstanceService()).willReturn(processInstanceService);
        given(serviceAccessor.getCommentService()).willReturn(commentService);
        given(serviceAccessor.getActivityInstanceService()).willReturn(activityInstanceService);
        given(serviceAccessor.getIdentityService()).willReturn(identityService);
        given(serviceAccessor.getActorMappingService()).willReturn(actorMappingService);
        given(serviceAccessor.getProfileService()).willReturn(profileService);
        given(serviceAccessor.getSupervisorService()).willReturn(supervisorService);
        given(serviceAccessor.getExternalIdentityMappingService()).willReturn(externalIdentityMappingService);
        given(serviceAccessor.getProcessDefinitionService()).willReturn(processDefinitionService);
        given(serviceAccessor.getDependencyResolver()).willReturn(dependencyResolver);

        given(userInfoDef1.getId()).willReturn(CUSTOM_USER_INFO_DEF_ID1);
        given(userInfoDef2.getId()).willReturn(CUSTOM_USER_INFO_DEF_ID2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deleOrganization_call_services_to_delete_all_organizationInfo() throws Exception {
        // given
        given(identityService.getCustomUserInfoDefinitions(0, PAGE_SIZE)).willReturn(Collections.singletonList(userInfoDef1),
                Collections.singletonList(userInfoDef2), Collections.<SCustomUserInfoDefinition> emptyList());

        // when
        organizationAPIImpl.deleteOrganization();

        // then
        verify(identityService, times(1)).deleteCustomUserInfoDefinition(CUSTOM_USER_INFO_DEF_ID1);
        verify(identityService, times(1)).deleteCustomUserInfoDefinition(CUSTOM_USER_INFO_DEF_ID2);
        verify(actorMappingService, times(1)).deleteAllActorMembers();
        verify(profileService, times(1)).deleteAllProfileMembers();
        verify(activityInstanceService, times(1)).deleteAllPendingMappings();
        verify(supervisorService, times(1)).deleteAllProcessSupervisors();
        verify(externalIdentityMappingService, times(1)).deleteAllExternalIdentityMappings();
        verify(identityService, times(1)).deleteAllUserMemberships();
        verify(identityService, times(1)).deleteAllGroups();
        verify(identityService, times(1)).deleteAllRoles();
        verify(identityService, times(1)).deleteAllUsers();
    }

}
