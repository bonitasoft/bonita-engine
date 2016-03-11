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


import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;

/**
 * @author Matthieu Chaffotte
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class SpringPlatformFileSystemBeanAccessor extends SpringFileSystemBeanAccessor {


    private final SpringPlatformInitFileSystemBeanAccessor parent;
    private AbsoluteFileSystemXmlApplicationContext context;

    public SpringPlatformFileSystemBeanAccessor(SpringPlatformInitFileSystemBeanAccessor parent) throws IOException, BonitaHomeNotSetException {
        this.parent = parent;
    }

    protected Properties getProperties() throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getPlatformProperties();
    }

    protected String[] getResources() throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getPlatformConfigurationFiles();
    }

    public <T> T getService(final Class<T> serviceClass) {
        return getContext().getBean(serviceClass);
    }

    protected <T> T getService(final String name, final Class<T> serviceClass) {
        return getContext().getBean(name, serviceClass);
    }

    protected <T> T getService(final String name) {
        return (T) getContext().getBean(name);
    }

    public void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

    public ApplicationContext getContext() {
        if (context == null) {
            try {
                ApplicationContext parentContext = null;
                if (parent != null) {
                    parentContext = parent.getContext();
                }
                context = new AbsoluteFileSystemXmlApplicationContext(getResources(), parentContext);
                final Properties properties = getProperties();
                context.addClassPathResource("bonita-platform-community.xml");
                context.addClassPathResource("bonita-platform-sp.xml");
                if (Boolean.valueOf(properties.getProperty("bonita.cluster", "false"))) {
                    context.addClassPathResource("bonita-platform-sp-cluster.xml");
                }

                final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
                configurer.setProperties(properties);
                context.addBeanFactoryPostProcessor(configurer);
                final String[] activeProfiles = getActiveProfiles();
                context.getEnvironment().setActiveProfiles(activeProfiles);
                context.refresh();
            } catch (IOException | BonitaHomeNotSetException e) {
                throw new BonitaRuntimeException(e);
            }
        }
        return context;
    }

    private String[] getActiveProfiles() throws IOException, BonitaHomeNotSetException {
        final Properties properties = BonitaHomeServer.getInstance().getPrePlatformInitProperties();
        final String activeProfiles = (String) properties.get("activeProfiles");
        return activeProfiles.split(",");
    }
}
