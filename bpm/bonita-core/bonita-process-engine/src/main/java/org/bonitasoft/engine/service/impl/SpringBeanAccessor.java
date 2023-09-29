/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.service.impl;

import static org.bonitasoft.engine.Profiles.CLUSTER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.commons.io.IOUtil;
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
public class SpringBeanAccessor {

    static final BonitaHomeServer BONITA_HOME_SERVER = BonitaHomeServer.getInstance();
    private BonitaSpringContext context;

    private boolean contextFinishedInitialized = false;
    private File bonita_conf;

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
        if (!contextFinishedInitialized) {
            initializeContext();
        }
        return context;
    }

    private synchronized void initializeContext() {
        if (contextFinishedInitialized) {
            return;
        }
        try {
            context = createContext();
            configureContext(context);
            context.refresh();
            contextFinishedInitialized = true;
        } catch (IOException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private void configureContext(BonitaSpringContext context) throws IOException {
        boolean isCluster = isCluster();
        for (String classPathResource : getSpringFileFromClassPath(isCluster)) {
            context.addClassPathResource(classPathResource);
        }
        if (isCluster) {
            context.getEnvironment().setActiveProfiles(CLUSTER);
        }
        for (BonitaConfiguration bonitaConfiguration : getConfigurationFromDatabase()) {
            context.addByteArrayResource(bonitaConfiguration);
        }

        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        propertySources.addFirst(new PropertiesPropertySource("contextProperties", getProperties()));
    }

    protected BonitaSpringContext createContext() {
        return new BonitaSpringContext(null, "Platform");
    }

    public void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
        contextFinishedInitialized = false;
    }

    protected Properties getProperties() throws IOException {
        Properties platformProperties = BONITA_HOME_SERVER.getPlatformProperties();
        platformProperties.putAll(BONITA_HOME_SERVER.getTenantProperties(1));
        platformProperties.setProperty("bonita.conf.folder", bonita_conf.getAbsolutePath());
        return platformProperties;
    }

    protected List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException {
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();

        List<BonitaConfiguration> platformConfiguration = BONITA_HOME_SERVER.getPlatformConfiguration();
        //handle special case for cache configuration files
        Iterator<BonitaConfiguration> iterator = platformConfiguration.iterator();
        bonita_conf = IOUtil.createTempDirectory(File.createTempFile("bonita_conf", "").toURI());
        bonita_conf.delete();
        bonita_conf.mkdir();
        while (iterator.hasNext()) {
            BonitaConfiguration bonitaConfiguration = iterator.next();
            if (bonitaConfiguration.getResourceName().contains("cache")) {
                iterator.remove();
                IOUtil.write(new File(bonita_conf, bonitaConfiguration.getResourceName()),
                        bonitaConfiguration.getResourceContent());
            }
        }

        bonitaConfigurations.addAll(platformConfiguration);
        bonitaConfigurations.addAll(BONITA_HOME_SERVER.getTenantConfiguration(1));
        return bonitaConfigurations;
    }

    protected List<String> getSpringFileFromClassPath(boolean cluster) {
        return List.of("bonita-community.xml", "bonita-subscription.xml");
    }

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
        return Boolean.parseBoolean(
                getPropertyWithPlaceholder(BONITA_HOME_SERVER.getPlatformProperties(), "bonita.cluster", "false"));
    }

}
