/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.engineclient;

/**
 * @author Vincent Elcrin
 */
public class EngineClientFactory {

    private final EngineAPIAccessor apiAccessor;

    public EngineClientFactory(final EngineAPIAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
    }

    public ProfileEngineClient createProfileEngineClient() {
        return new ProfileEngineClient(apiAccessor.getProfileAPI());
    }

    public ProfileMemberEngineClient createProfileMemberEngineClient() {
        return new ProfileMemberEngineClient(apiAccessor.getProfileAPI());
    }

    public ProcessEngineClient createProcessEngineClient() {
        return new ProcessEngineClient(apiAccessor.getProcessAPI());
    }

    public CaseEngineClient createCaseEngineClient() {
        return new CaseEngineClient(apiAccessor.getProcessAPI());
    }

    public HumanTaskEngineClient createHumanTaskEngineClient() {
        return new HumanTaskEngineClient(apiAccessor.getProcessAPI());
    }

    public ActivityEngineClient createActivityEngineClient() {
        return new ActivityEngineClient(apiAccessor.getProcessAPI());
    }

    public UserEngineClient createUserEngineClient() {
        return new UserEngineClient(apiAccessor.getIdentityAPI());
    }

    public GroupEngineClient createGroupEngineClient() {
        return new GroupEngineClient(apiAccessor.getGroupAPI());
    }

    public TenantManagementEngineClient createTenantManagementEngineClient() {
        return new TenantManagementEngineClient(apiAccessor.getTenantAdministrationAPI());
    }
}
