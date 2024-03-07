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
package org.bonitasoft.engine.cache.ehcache;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class CacheConfigurationIT extends CommonBPMServicesTest {

    EhCacheCacheService cacheService;

    @Before
    public void setUp() {
        cacheService = (EhCacheCacheService) getServiceAccessor().getPlatformCacheService();
    }

    @Test
    public void all_required_cache_configurations_should_exist() {
        assertThat(cacheService.getCacheConfigurationNames()).containsExactlyInAnyOrder(
                "CONFIGURATION_FILES_CACHE",
                "USER_FILTER",
                "transient_data",
                "GROOVY_SCRIPT_CACHE_NAME",
                "_PROCESSDEF",
                "SYNCHRO_SERVICE_CACHE",
                "parameters",
                "DEFAULT_PLATFORM",
                "CONNECTOR");
    }
}
