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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * Spring bean accessor that get its configuration from configuration file in classpath and in database
 *
 * @author Charles Souillard
 */
@Slf4j
public class SpringBeanAccessor {

    private static final String HAZELCAST_CONFIG_FILENAME = "hazelcast.xml";

    static final BonitaHomeServer BONITA_HOME_SERVER = BonitaHomeServer.getInstance();

    private static final String WORK_CORE_POOL_SIZE = "bonita.tenant.work.corePoolSize";
    private static final String WORK_MAX_POOL_SIZE = "bonita.tenant.work.maximumPoolSize";
    private static final String WORK_KEEP_ALIVE_IN_SECONDS = "bonita.tenant.work.keepAliveTimeSeconds";
    private static final String WORK_SQLSERVER_DELAY_ON_MULTIPLE_XA_RESOURCE = "bonita.tenant.work.sqlserver.delayOnMultipleXAResource";
    private static final String WORK_MYSQL_DELAY_ON_MULTIPLE_XA_RESOURCE = "bonita.tenant.work.mysql.delayOnMultipleXAResource";
    private static final String WORK_ORACLE_DELAY_ON_MULTIPLE_XA_RESOURCE = "bonita.tenant.work.oracle.delayOnMultipleXAResource";
    private static final String CONNECTOR_CORE_POOL_SIZE = "bonita.tenant.connector.corePoolSize";
    private static final String CONNECTOR_MAX_POOL_SIZE = "bonita.tenant.connector.maximumPoolSize";
    private static final String CONNECTOR_KEEP_ALIVE_IN_SECONDS = "bonita.tenant.connector.keepAliveTimeSeconds";

    private BonitaSpringContext context;

    private boolean contextFinishedInitialized = false;

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
        final boolean legacyMode = context.getEnvironment().getProperty("bonita.runtime.properties.order.legacy-mode",
                boolean.class, false);
        if (legacyMode) {
            // continue to have properties files from database with the higher priority order.
            propertySources.addFirst(new PropertiesPropertySource("contextProperties", getProperties()));
        } else {
            // Make values from database be easily overridable with default Spring mechanism.
            // This is achieved by adding Bonita properties from database with a priority just AFTER
            // OS environment variables and Java System properties.
            // For default Spring property order, see
            // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
            propertySources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    new PropertiesPropertySource("contextProperties", getProperties()));
        }
        warnDeprecatedProperties(propertySources);
    }

    protected void warnDeprecatedProperties(MutablePropertySources propertySources) {
        warnIfPropertyIsDeprecated(propertySources, WORK_CORE_POOL_SIZE);
        warnIfPropertyIsDeprecated(propertySources, WORK_MAX_POOL_SIZE);
        warnIfPropertyIsDeprecated(propertySources, WORK_KEEP_ALIVE_IN_SECONDS);
        warnIfPropertyIsDeprecated(propertySources, WORK_SQLSERVER_DELAY_ON_MULTIPLE_XA_RESOURCE);
        warnIfPropertyIsDeprecated(propertySources, WORK_MYSQL_DELAY_ON_MULTIPLE_XA_RESOURCE);
        warnIfPropertyIsDeprecated(propertySources, WORK_ORACLE_DELAY_ON_MULTIPLE_XA_RESOURCE);
        warnIfPropertyIsDeprecated(propertySources, CONNECTOR_CORE_POOL_SIZE);
        warnIfPropertyIsDeprecated(propertySources, CONNECTOR_MAX_POOL_SIZE);
        warnIfPropertyIsDeprecated(propertySources, CONNECTOR_KEEP_ALIVE_IN_SECONDS);
    }

    private void warnIfPropertyIsDeprecated(MutablePropertySources propertySources, String property) {
        propertySources.stream()
                .filter(ps -> ps.containsProperty(property))
                .map(ps -> ps.getProperty(property))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(value -> log.warn(
                        "{} property is not supported in community edition anymore. It will be ignored.",
                        property));
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
        platformProperties.putAll(BONITA_HOME_SERVER.getTenantProperties(BONITA_HOME_SERVER.getDefaultTenantId()));
        return platformProperties;
    }

    protected List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException {
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();

        List<BonitaConfiguration> platformConfiguration = BONITA_HOME_SERVER.getPlatformConfiguration();

        extractHazelcastConfigurationFile(platformConfiguration);

        bonitaConfigurations.addAll(platformConfiguration);
        bonitaConfigurations.addAll(BONITA_HOME_SERVER.getTenantConfiguration(BONITA_HOME_SERVER.getDefaultTenantId()));
        return bonitaConfigurations;
    }

    private static void extractHazelcastConfigurationFile(List<BonitaConfiguration> platformConfiguration)
            throws IOException {
        // handle special case for Hazelcast configuration file:
        Iterator<BonitaConfiguration> iterator = platformConfiguration.iterator();
        while (iterator.hasNext()) {
            BonitaConfiguration bonitaConfiguration = iterator.next();
            if (HAZELCAST_CONFIG_FILENAME.equals(bonitaConfiguration.getResourceName())) {
                iterator.remove();
                final File hzConfigFile = new File(IOUtil.TMP_DIRECTORY, HAZELCAST_CONFIG_FILENAME);
                if (!hzConfigFile.exists()) {
                    Files.write(hzConfigFile.toPath(), bonitaConfiguration.getResourceContent());
                    hzConfigFile.deleteOnExit();
                }
                String hazelcastConfigFile = hzConfigFile.getAbsolutePath();

                // Allow to preserve "hazelcast.config" if already passed as System property:
                if (!System.getProperties().containsKey("hazelcast.config")) {
                    log.info("Setting sysprop 'hazelcast.config' to {}", hazelcastConfigFile);
                    System.setProperty("hazelcast.config", hazelcastConfigFile);
                    System.setProperty("hibernate.javax.cache.uri", new File(hazelcastConfigFile).toURI().toString());
                } else {
                    log.info("Sysprop 'hazelcast.config' already set to '{}'. Preserving this value.",
                            System.getProperty("hazelcast.config"));
                }
                return; // found, no need to go further
            }
        }
    }

    protected List<String> getSpringFileFromClassPath(boolean cluster) {
        return List.of("bonita-community.xml", "bonita-subscription.xml");
    }

    String getPropertyWithPlaceholder(Properties properties, String key, String defaultValue) {
        String property = properties.getProperty(key, defaultValue);
        if (property.startsWith("${") && property.endsWith("}")) {
            property = property.substring(2, property.length() - 1);
            String sysPropertyKey = property.substring(0, property.indexOf(':'));
            String sysPropertyDefaultValue = property.substring(property.indexOf(':') + 1);
            return System.getProperty(sysPropertyKey, sysPropertyDefaultValue);
        }
        return property;
    }

    protected boolean isCluster() throws IOException {
        return Boolean.parseBoolean(
                getPropertyWithPlaceholder(BONITA_HOME_SERVER.getPlatformProperties(), "bonita.cluster", "false"));
    }

}
