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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class RefreshTenantClassLoaders implements TransactionContent {

    private final TenantServiceAccessor tenantServiceAccessor;

    private final Long tenantId;

    public RefreshTenantClassLoaders(final TenantServiceAccessor tenantServiceAccessor, final Long tenantId) {
        this.tenantServiceAccessor = tenantServiceAccessor;
        this.tenantId = tenantId;
    }

    @Override
    public void execute() throws SBonitaException {
        // set tenant classloader
        final int maxResults = 100;
        final DependencyService dependencyService = tenantServiceAccessor.getDependencyService();
        dependencyService.refreshClassLoader(ScopeType.TENANT, tenantId);
        final ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        List<Long> processDefinitionIds;
        int j = 0;
        do {
            processDefinitionIds = processDefinitionService.getProcessDefinitionIds(j, maxResults);
            j += maxResults;
            for (final Long id : processDefinitionIds) {
                dependencyService.refreshClassLoader(ScopeType.PROCESS, id);
            }
        } while (processDefinitionIds.size() == maxResults);
    }

}
