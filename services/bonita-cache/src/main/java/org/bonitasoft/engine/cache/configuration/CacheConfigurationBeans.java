/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.cache.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class that defines cache configuration beans for the Bonita platform.
 * <p>
 * This class provides factory methods for creating {@link org.bonitasoft.engine.cache.CacheConfiguration}
 * beans with their respective properties loaded from application configuration files.
 * Each cache configuration is parameterized through Spring's {@code @Value} annotations,
 * allowing runtime configuration via properties files.
 * </p>
 * <p>
 * The configured caches include:
 * </p>
 * <ul>
 * <li><b>Application Token Cache</b> - Stores application authentication tokens</li>
 * <li><b>Default Platform Cache</b> - General-purpose platform-level cache</li>
 * <li><b>Synchro Service Cache</b> - Synchronization service cache</li>
 * <li><b>Config Files Cache</b> - Configuration files cache</li>
 * <li><b>Connector Cache</b> - Connector definitions and instances</li>
 * <li><b>Process Definition Cache</b> - BPMN process definitions</li>
 * <li><b>Parameter Cache</b> - Process and tenant parameters</li>
 * <li><b>User Filter Cache</b> - User filter definitions</li>
 * <li><b>Groovy Script Cache</b> - Compiled Groovy scripts</li>
 * <li><b>Transient Data Cache</b> - Transient process data</li>
 * </ul>
 *
 * @see org.bonitasoft.engine.cache.CacheConfiguration
 */
@Configuration
public class CacheConfigurationBeans {

    public static final String APPLICATION_TOKEN_CACHE_NAME = "application-token";

    /**
     * Creates the cache configuration for application tokens.
     * <p>
     * This cache stores tokens used for applications.
     * Configuration properties are loaded from {@code bonita.runtime.cache.application-token.*}.
     * </p>
     *
     * @param maxElementsInMemory the maximum number of elements that can be stored in memory
     * @param eternal whether cached elements never expire
     * @param evictionPolicy the eviction policy (e.g., "LRU", "LFU", "FIFO")
     * @param readIntensive whether this cache is optimized for read-intensive workloads
     * @param timeToLiveSeconds the time-to-live in seconds for cached elements
     * @param offHeapSizeMB the size of off-heap memory in MB (0 = no off-heap)
     * @return the configured cache configuration bean
     */
    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration applicationTokenCacheConfiguration(
            @Value("${bonita.runtime.cache.application-token.maxElementsInMemory:1000}") final int maxElementsInMemory,
            @Value("${bonita.runtime.cache.application-token.eternal:false}") final boolean eternal,
            @Value("${bonita.runtime.cache.application-token.evictionPolicy:LRU}") final String evictionPolicy,
            @Value("${bonita.runtime.cache.application-token.readIntensive:false}") final boolean readIntensive,
            @Value("${bonita.runtime.cache.application-token.timeToLiveSeconds:3600}") final int timeToLiveSeconds,
            @Value("${bonita.runtime.cache.application-token.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration(APPLICATION_TOKEN_CACHE_NAME, maxElementsInMemory, eternal, evictionPolicy,
                readIntensive, timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration defaultCacheConfiguration(
            @Value("${bonita.platform.cache.default.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.platform.cache.default.eternal}") final boolean eternal,
            @Value("${bonita.platform.cache.default.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.platform.cache.default.readIntensive}") final boolean readIntensive,
            @Value("${bonita.platform.cache.default.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.platform.cache.default.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("DEFAULT_PLATFORM", maxElementsInMemory, eternal, evictionPolicy,
                readIntensive, timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration synchroServiceCacheConfig(
            @Value("${bonita.platform.cache.synchro.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.platform.cache.synchro.eternal}") final boolean eternal,
            @Value("${bonita.platform.cache.synchro.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.platform.cache.synchro.readIntensive}") final boolean readIntensive,
            @Value("${bonita.platform.cache.synchro.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.platform.cache.synchro.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("SYNCHRO_SERVICE_CACHE", maxElementsInMemory, eternal, evictionPolicy,
                readIntensive, timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration configFilesCacheConfig(
            @Value("${bonita.platform.cache.configfiles.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.platform.cache.configfiles.eternal}") final boolean eternal,
            @Value("${bonita.platform.cache.configfiles.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.platform.cache.configfiles.readIntensive}") final boolean readIntensive,
            @Value("${bonita.platform.cache.configfiles.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.platform.cache.configfiles.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("CONFIGURATION_FILES_CACHE", maxElementsInMemory, eternal, evictionPolicy,
                readIntensive, timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration connectorCacheConfig(
            @Value("${bonita.tenant.cache.connector.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.connector.eternal}") final boolean eternal,
            @Value("${bonita.tenant.cache.connector.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.connector.readIntensive}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.connector.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.connector.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("CONNECTOR", maxElementsInMemory, eternal, evictionPolicy, readIntensive,
                timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration processDefinitionCacheConfig(
            @Value("${bonita.tenant.cache.processdef.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.processdef.eternal}") final boolean eternal,
            @Value("${bonita.tenant.cache.processdef.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.processdef.readIntensive}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.processdef.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.processdef.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("_PROCESSDEF", maxElementsInMemory, eternal, evictionPolicy, readIntensive,
                timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration parameterCacheConfig(
            @Value("${bonita.tenant.cache.parameter.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.parameter.eternal}") final boolean eternal,
            @Value("${bonita.tenant.cache.parameter.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.parameter.readIntensive}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.parameter.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.parameter.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("parameters", maxElementsInMemory, eternal, evictionPolicy, readIntensive,
                timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration userFilterCacheConfig(
            @Value("${bonita.tenant.cache.userfilter.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.userfilter.eternal}") final boolean eternal,
            @Value("${bonita.tenant.cache.userfilter.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.userfilter.readIntensive}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.userfilter.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.userfilter.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("USER_FILTER", maxElementsInMemory, eternal, evictionPolicy, readIntensive,
                timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration groovyScriptCacheConfig(
            @Value("${bonita.tenant.cache.groovy.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.groovy.eternal}") final boolean eternal,
            @Value("${bonita.tenant.cache.groovy.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.groovy.readIntensive}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.groovy.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.groovy.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("GROOVY_SCRIPT_CACHE_NAME", maxElementsInMemory, eternal, evictionPolicy,
                readIntensive, timeToLiveSeconds, offHeapSizeMB);
    }

    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration transientDataCacheConfig(
            @Value("${bonita.tenant.cache.transientdata.maxElementsInMemory}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.transientdata.eternal}") final boolean eternal,
            @Value("${bonita.tenant.cache.transientdata.evictionPolicy}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.transientdata.readIntensive}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.transientdata.timeToLiveSeconds}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.transientdata.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("transient_data", maxElementsInMemory, eternal, evictionPolicy, readIntensive,
                timeToLiveSeconds, offHeapSizeMB);
    }

    /**
     * Cache for the per-delegate active delegation-rule set, read on the permission hot path.
     * The region name must match
     * {@code DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE} ("active_delegation_rules").
     * In cluster mode it is kept node-local (see {@code EngineClusterConfiguration}) so reads stay
     * in-JVM; cross-node invalidation is therefore bounded by {@code timeToLiveSeconds}.
     */
    @Bean
    public org.bonitasoft.engine.cache.CacheConfiguration delegationRulesCacheConfig(
            @Value("${bonita.tenant.cache.delegation.maxElementsInMemory:10000}") final int maxElementsInMemory,
            @Value("${bonita.tenant.cache.delegation.eternal:false}") final boolean eternal,
            @Value("${bonita.tenant.cache.delegation.evictionPolicy:LRU}") final String evictionPolicy,
            @Value("${bonita.tenant.cache.delegation.readIntensive:true}") final boolean readIntensive,
            @Value("${bonita.tenant.cache.delegation.timeToLiveSeconds:300}") final int timeToLiveSeconds,
            @Value("${bonita.tenant.cache.delegation.offHeapSizeMB:0}") final int offHeapSizeMB) {
        return createCacheConfiguration("active_delegation_rules", maxElementsInMemory, eternal, evictionPolicy,
                readIntensive, timeToLiveSeconds, offHeapSizeMB);
    }

    private org.bonitasoft.engine.cache.CacheConfiguration createCacheConfiguration(String name,
            int maxElementsInMemory, boolean eternal, String evictionPolicy, boolean readIntensive,
            int timeToLiveSeconds, int offHeapSizeMB) {
        var cacheConfiguration = new org.bonitasoft.engine.cache.CacheConfiguration();
        cacheConfiguration.setName(name);
        cacheConfiguration.setMaxElementsInMemory(maxElementsInMemory);
        cacheConfiguration.setEternal(eternal);
        cacheConfiguration.setEvictionPolicy(evictionPolicy);
        cacheConfiguration.setReadIntensive(readIntensive);
        cacheConfiguration.setTimeToLiveSeconds(timeToLiveSeconds);
        cacheConfiguration.setOffHeapSizeMB(offHeapSizeMB);
        return cacheConfiguration;
    }
}
