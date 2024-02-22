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
package org.bonitasoft.web.rest.server.api.deployer;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.datastore.profile.GetProfileHelper;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.rest.server.engineclient.EngineClientFactory;
import org.bonitasoft.web.rest.server.framework.Deployer;

/**
 * @author Vincent Elcrin
 */
public class DeployerFactory {

    private final APISession apiSession;

    private final EngineClientFactory factory;

    public DeployerFactory(final APISession apiSession) {
        this.apiSession = apiSession;
        factory = new EngineClientFactory(new EngineAPIAccessor(apiSession));
    }

    public UserDeployer createUserDeployer(final String attribute) {
        return new UserDeployer(new UserDatastore(apiSession), attribute);
    }

    public Deployer createProfileDeployer(final String attribute) {
        return new GenericDeployer<>(createProfileGetter(), attribute);
    }

    private GetProfileHelper createProfileGetter() {
        return new GetProfileHelper(factory.createProfileEngineClient());
    }

}
