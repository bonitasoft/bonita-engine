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
package org.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.support.FileSystemXmlApplicationContext;


@PrepareForTest({ SpringPlatformFileSystemBeanAccessor.class })
@RunWith(PowerMockRunner.class)
public class SpringTenantFileSystemBeanAccessorTest {

    final long tenantId = 1;

    @Spy
    SpringTenantFileSystemBeanAccessor springTenantFileSystemBeanAccessor = new SpringTenantFileSystemBeanAccessor(tenantId);

    @Mock
    AbsoluteFileSystemXmlApplicationContext absoluteFileSystemXmlApplicationContext;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testInitializeContextShouldExecuteCorrectly() throws Exception {
        Properties props = new Properties();
        String authenticationServiceValue = "genericAuthenticationService";
        String authenticationServiceKey = "authentication.service";
        props.put(authenticationServiceKey, authenticationServiceValue);
        PowerMockito.mockStatic(SpringPlatformFileSystemBeanAccessor.class);
        doReturn(absoluteFileSystemXmlApplicationContext).when(springTenantFileSystemBeanAccessor).createFileSystemApplicationContext(
                any(FileSystemXmlApplicationContext.class));
        doReturn(props).when(springTenantFileSystemBeanAccessor).findBonitaServerTenantProperties(tenantId);
        springTenantFileSystemBeanAccessor.initializeContext(null);
        assertThat(props).containsEntry(SpringTenantFileSystemBeanAccessor.TENANT_ID, String.valueOf(tenantId)).containsEntry(authenticationServiceKey,
                authenticationServiceValue);
        verify(springTenantFileSystemBeanAccessor, times(1)).findBonitaServerTenantProperties(tenantId);
        verify(springTenantFileSystemBeanAccessor, times(1)).createFileSystemApplicationContext(any(FileSystemXmlApplicationContext.class));
        verify(absoluteFileSystemXmlApplicationContext, times(1)).refresh();
        assertThat(springTenantFileSystemBeanAccessor.context).isSameAs(absoluteFileSystemXmlApplicationContext);
    }

}
