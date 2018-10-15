/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;

/**
 * @author Matthieu Chaffotte
 * @author Charles Souillard
 */
public class PlatformInitBeanAccessor extends SpringBeanAccessor {

    PlatformInitBeanAccessor() {
        super(null);
    }

    @Override
    protected Properties getProperties() {
        return new Properties();
    }

    @Override
    protected List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException {
        return BONITA_HOME_SERVER.getPlatformInitConfiguration();
    }

    @Override
    protected List<String> getSpringFileFromClassPath(boolean cluster) {
        List<String> resources = new ArrayList<>();
        resources.add("bonita-platform-init-community.xml");
        resources.add("bonita-platform-init-sp.xml");
        return resources;
    }

}
