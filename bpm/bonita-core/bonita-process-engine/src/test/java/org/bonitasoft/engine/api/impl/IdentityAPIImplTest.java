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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentityAPIImplTest {

    @Mock
    private TenantServiceAccessor tenantAccessor;

    @Mock
    private IdentityService identityService;

    @Spy
    private IdentityAPIImpl identityAPI;

    @Before
    public void setUp() {
        doReturn(tenantAccessor).when(identityAPI).getTenantAccessor();
        given(tenantAccessor.getIdentityService()).willReturn(identityService);
    }

    @Test
    public void getUserIdsWithCustomUserInfo_returns_the_value_returned_by_service() throws Exception {
        //given
        given(identityService.getUserIdsWithCustomUserInfo("skills", "Java", false, 0, 10)).willReturn(Arrays.asList(25L, 40L));

        //when
        final List<Long> userIds = identityAPI.getUserIdsWithCustomUserInfo("skills", "Java", false, 0, 10);

        //then
        assertThat(userIds).containsExactly(25L, 40L);
    }

    @Test(expected = RetrieveException.class)
    //then
    public void getUserIdsWithCustomUserInfo_throws_RetriveException_when_service_throws_SBonitaException() throws Exception {
        //given
        given(identityService.getUserIdsWithCustomUserInfo(anyString(), anyString(), anyBoolean(), anyInt(), anyInt())).willThrow(new SIdentityException(""));

        //when
        identityAPI.getUserIdsWithCustomUserInfo("skills", "Java", true, 0, 10);
    }

}
