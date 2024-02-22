/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.benchmarks;

import org.bonitasoft.engine.authorization.properties.ConfigurationFilesManager;
import org.bonitasoft.engine.authorization.properties.ResourcesPermissionsMapping;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.test.TestEngine;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class PermissionCachingBenchmark {

    private TestEngine engine;
    private ResourcesPermissionsMapping resourcesPermissionsMapping;
    private CacheService cacheService;

    @Setup
    public void setup() throws Exception {
        engine = TestEngineImpl.getInstance();
        engine.start();
        cacheService = ServiceAccessorSingleton.getInstance().getCacheService();
        resourcesPermissionsMapping = new ResourcesPermissionsMapping(1L, cacheService,
                new ConfigurationFilesManager());
    }

    @TearDown
    public void tearDown() throws Exception {
        engine.stop();
    }

    @Benchmark
    public void callPermissionWithCache() {
        resourcesPermissionsMapping.getResourcePermissions("GET", "bpm", "case");
    }

    @Benchmark
    public void callPermissionFromCache50times() {
        for (int i = 0; i < 50; i++) {
            resourcesPermissionsMapping.getResourcePermissions("GET", "bpm", "case");
        }
    }
}
