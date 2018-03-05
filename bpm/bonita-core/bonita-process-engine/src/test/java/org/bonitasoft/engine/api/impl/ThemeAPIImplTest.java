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
import static org.mockito.Mockito.doReturn;

import java.util.Date;

import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.ThemeType;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ThemeAPIImplTest {

    @Mock
    private ThemeService themeService;
    @Mock
    private TenantServiceAccessor tenantAccessor;
    @Spy
    private ThemeAPIImpl themeAPI;


    @Before
    public void before(){
        doReturn(tenantAccessor).when(themeAPI).getTenantAccessor();
        doReturn(themeService).when(tenantAccessor).getThemeService();
    }

    @Test
    public void shouldBeAvalableWhenTenantIsPause() throws Exception {
        assertThat(ThemeAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as("should theme api be available when tenant is paused")
                .isTrue();
    }

    @Test
    public void should_getLastUpdateDate_call_themeService_method() throws SBonitaReadException {
        final long timestamp = 1234;
        final Date date = new Date(timestamp);
        doReturn(timestamp).when(themeService).getLastUpdateDate(SThemeType.MOBILE);

        final Date lastUpdateDate = themeAPI.getLastUpdateDate(ThemeType.MOBILE);

        assertThat(lastUpdateDate).isEqualTo(date);
    }



}
