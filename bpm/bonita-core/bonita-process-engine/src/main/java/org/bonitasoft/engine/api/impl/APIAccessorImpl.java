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

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ThemeAPI;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Baptiste Mesta
 */
public class APIAccessorImpl implements APIAccessor {

    private static final long serialVersionUID = -3602975597536895697L;

    @Override
    public IdentityAPI getIdentityAPI() {
        return new IdentityAPIImpl();
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return new ProcessAPIImpl();
    }

    @Override
    public CommandAPI getCommandAPI() {
        return new CommandAPIImpl();
    }

    @Override
    public ProfileAPI getProfileAPI() {
        return new ProfileAPIImpl();
    }

    @Override
    public ThemeAPI getThemeAPI() {
        return new ThemeAPIImpl();
    }

    @Override
    public PermissionAPI getPermissionAPI() {
        return new PermissionAPIImpl();
    }

    public PageAPI getCustomPageAPI() {
        return new PageAPIImpl();
    }

    @Override
    public ApplicationAPI getLivingApplicationAPI() {
        return new ApplicationAPIImpl();
    }

    @Override
    public ProcessConfigurationAPI getProcessConfigurationAPI() {
        return new ProcessConfigurationAPIImpl();
    }

    @Override
    public BusinessDataAPI getBusinessDataAPI() {
        return new BusinessDataAPIImpl();
    }
}
