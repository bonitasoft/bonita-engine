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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.springframework.context.ApplicationContext;

/**
 * @author Matthieu Chaffotte
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class PlatformBeanAccessor extends SpringBeanAccessor {

    private File bonita_conf;

    PlatformBeanAccessor(ApplicationContext parent) {
        super(parent);
    }

    @Override
    protected Properties getProperties() throws IOException {
        Properties platformProperties = BONITA_HOME_SERVER.getPlatformProperties();
        platformProperties.setProperty("bonita.conf.folder", bonita_conf.getAbsolutePath());
        return platformProperties;
    }

    @Override
    protected List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException {
        List<BonitaConfiguration> platformConfiguration = BONITA_HOME_SERVER.getPlatformConfiguration();
        //handle special case for cache configuration files
        Iterator<BonitaConfiguration> iterator = platformConfiguration.iterator();
        bonita_conf = org.bonitasoft.engine.io.IOUtil.createTempDirectory(File.createTempFile("bonita_conf", "").toURI());
        bonita_conf.delete();
        bonita_conf.mkdir();
        while (iterator.hasNext()) {
            BonitaConfiguration bonitaConfiguration = iterator.next();
            if (bonitaConfiguration.getResourceName().contains("cache")) {
                iterator.remove();
                IOUtil.write(new File(bonita_conf, bonitaConfiguration.getResourceName()), bonitaConfiguration.getResourceContent());
            }
        }
        return platformConfiguration;
    }

    @Override
    protected List<String> getSpringFileFromClassPath(boolean cluster) {
        ArrayList<String> resources = new ArrayList<>();
        resources.add("bonita-platform-community.xml");
        resources.add("bonita-platform-sp.xml");
        if (cluster) {
            resources.add("bonita-platform-sp-cluster.xml");
        }
        return resources;
    }

}
