/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.service.PlatformServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class RefreshPlatformClassLoader implements TransactionContentWithResult<List<Long>> {

    private final PlatformServiceAccessor platformAccessor;

    private List<Long> tenantIds;

    public RefreshPlatformClassLoader(final PlatformServiceAccessor platformAccessor) {
        this.platformAccessor = platformAccessor;
    }

    @Override
    public void execute() throws SBonitaException {
        final DependencyService platformDependencyService = platformAccessor.getDependencyService();
        final ClassLoaderService classLoaderService = platformAccessor.getClassLoaderService();
        platformDependencyService.refreshClassLoader(classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        final PlatformService platformService = platformAccessor.getPlatformService();
        List<STenant> tenants;
        final int maxResults = 100;
        int i = 0;
        tenantIds = new ArrayList<Long>();
        do {
            tenants = platformService.getTenants(new QueryOptions(i, maxResults));
            i += maxResults;
            for (final STenant sTenant : tenants) {
                tenantIds.add(sTenant.getId());
            }
        } while (tenants.size() == maxResults);
        // reput the platform in cache at the node start
        platformService.cachePlatform();
    }

    @Override
    public List<Long> getResult() {
        return tenantIds;
    }

}
