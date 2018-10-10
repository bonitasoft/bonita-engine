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
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Spring bean accessor that get its configuration from configuration file in classpath and in database
 *
 * @author Charles Souillard
 */
public abstract class SpringBeanAccessor {

    static final BonitaHomeServer BONITA_HOME_SERVER = BonitaHomeServer.getInstance();
    private final ApplicationContext parent;
    private BonitaSpringContext context;


    public SpringBeanAccessor(ApplicationContext parent) {
        this.parent = parent;
    }

    public <T> T getService(final Class<T> serviceClass) {
        return getContext().getBean(serviceClass);
    }

    <T> T getService(String name, Class<T> clazz) {
        return getContext().getBean(name, clazz);
    }

    <T> T getService(String serviceName) {
        return (T) getContext().getBean(serviceName);
    }

    public ApplicationContext getContext() {
        if (context == null) {
            try {
                context = createContext();
                configureContext(context);
                context.refresh();
            } catch (IOException e) {
                throw new BonitaRuntimeException(e);
            }
        }
        return context;
    }

    private void configureContext(BonitaSpringContext context) throws IOException {
        for (String classPathResource : getSpringFileFromClassPath(isCluster())) {
            context.addClassPathResource(classPathResource);
        }
        for (BonitaConfiguration bonitaConfiguration : getConfigurationFromDatabase()) {
            context.addByteArrayResource(bonitaConfiguration);
        }

        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        propertySources.addFirst(new PropertiesPropertySource("contextProperties", getProperties()));
    }

    protected BonitaSpringContext createContext() {
        return new BonitaSpringContext(parent);
    }

    public void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

    protected abstract Properties getProperties() throws IOException;

    protected abstract List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException;

    protected abstract List<String> getSpringFileFromClassPath(boolean cluster);

    String getPropertyWithPlaceholder(Properties properties, String key, String defaultValue) {
        String property = properties.getProperty(key, defaultValue);
        if (property.startsWith("${") && property.endsWith("}")) {
            property = property.substring(2, property.length() - 1);
            String sysPropertyKey = property.substring(0, property.indexOf(':'));
            String sysPropertyDefaultValue = property.substring(property.indexOf(':') + 1, property.length());
            return System.getProperty(sysPropertyKey, sysPropertyDefaultValue);
        }
        return property;
    }

    protected boolean isCluster() throws IOException {
        return Boolean.valueOf(getPropertyWithPlaceholder(BONITA_HOME_SERVER.getPlatformProperties(), "bonita.cluster", "false"));
    }

}
