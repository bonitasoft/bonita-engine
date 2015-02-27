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
package org.bonitasoft.engine.api.impl.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Baptiste Mesta
 * 
 */
public class GetTenantsCallable implements Callable<List<STenant>> {

    /**
     * 
     */
    private final PlatformService platformService;

    /**
     * @param platformService
     */
    public GetTenantsCallable(final PlatformService platformService) {
        this.platformService = platformService;
    }

    @Override
    public List<STenant> call() throws Exception {
        List<STenant> tenants;
        final int maxResults = 100;
        int i = 0;
        final List<STenant> tenantIds = new ArrayList<STenant>();
        do {
            tenants = platformService.getTenants(new QueryOptions(i, maxResults, STenant.class, "id", OrderByType.ASC));
            i += maxResults;
            for (final STenant sTenant : tenants) {
                tenantIds.add(sTenant);
            }
        } while (tenants.size() == maxResults);
        return tenantIds;
    }
}
