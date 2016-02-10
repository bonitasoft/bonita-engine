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

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APIAccessorImplTest {

    private APIAccessorImpl apiAccessor;

    private void initAPIAccessor() {
        apiAccessor = new APIAccessorImpl();
    }

    @Test
    public void getIdentityAPI_should_return_the_default_implementation() {
        initAPIAccessor();
        final IdentityAPI identityAPI = apiAccessor.getIdentityAPI();

        assertThat(identityAPI).isNotNull().isExactlyInstanceOf(IdentityAPIImpl.class);
    }

    @Test
    public void getProcessAPI_should_return_the_default_implementation() {
        initAPIAccessor();
        final ProcessAPI processAPI = apiAccessor.getProcessAPI();

        assertThat(processAPI).isNotNull().isExactlyInstanceOf(ProcessAPIImpl.class);
    }

    @Test
    public void getCommandAPI_should_return_the_default_implementation() {
        initAPIAccessor();
        final CommandAPI commandAPI = apiAccessor.getCommandAPI();

        assertThat(commandAPI).isNotNull().isExactlyInstanceOf(CommandAPIImpl.class);
    }

    @Test
    public void getProfileAPI_should_return_the_default_implementation() {
        initAPIAccessor();
        final ProfileAPI profileAPI = apiAccessor.getProfileAPI();

        assertThat(profileAPI).isNotNull().isExactlyInstanceOf(ProfileAPIImpl.class);
    }

    @Test
    public void getThemeAPI_should_return_the_default_implementation() {
        initAPIAccessor();
        final ThemeAPI themeAPI = apiAccessor.getThemeAPI();

        assertThat(themeAPI).isNotNull().isExactlyInstanceOf(ThemeAPIImpl.class);
    }

    @Test
    public void getPageAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final org.bonitasoft.engine.api.PageAPI pageAPI = apiAccessor.getCustomPageAPI();

        assertThat(pageAPI).isNotNull().isExactlyInstanceOf(PageAPIImpl.class);
    }

    @Test
    public void getApplicationAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ApplicationAPI applicationAPI = apiAccessor.getLivingApplicationAPI();

        assertThat(applicationAPI).isNotNull().isExactlyInstanceOf(ApplicationAPIImpl.class);
    }

}
