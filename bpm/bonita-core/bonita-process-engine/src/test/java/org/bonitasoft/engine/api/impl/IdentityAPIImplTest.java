package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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
    public void setUp() throws Exception {
        doReturn(tenantAccessor).when(identityAPI).getTenantAccessor();
        given(tenantAccessor.getIdentityService()).willReturn(identityService);
    }
    
    @Test
    public void getUserIdsWithCustomUserInfo_returns_the_value_returned_by_service() throws Exception {
        //given
        given(identityService.getUserIdsWithCustomUserInfo("skills", "Java", 0, 10)).willReturn(Arrays.asList(25L, 40L));
        
        //when
        List<Long> userIds = identityAPI.getUserIdsWithCustomUserInfo("skills", "Java", 0, 10);

        //then
        assertThat(userIds).containsExactly(25L, 40L);
    }

    @Test(expected = RetrieveException.class) //then
    public void getUserIdsWithCustomUserInfo_throws_RetriveException_when_service_throws_SBonitaException() throws Exception {
        //given
        given(identityService.getUserIdsWithCustomUserInfo(anyString(), anyString(), anyInt(), anyInt())).willThrow(new SIdentityException(""));
        
        //when
        identityAPI.getUserIdsWithCustomUserInfo("skills", "Java", 0, 10);
    }

}
