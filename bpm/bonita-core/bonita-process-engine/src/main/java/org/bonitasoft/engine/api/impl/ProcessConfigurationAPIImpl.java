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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.form.mapping.FormMapping;
import org.bonitasoft.engine.form.mapping.FormMappingType;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Baptiste Mesta
 */
public class ProcessConfigurationAPIImpl implements ProcessConfigurationAPI {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public SearchResult<FormMapping> searchFormMappings(final SearchOptions searchOptions) {
        return null;
    }

    @Override
    public FormMapping getProcessStartForm(final long processDefinitionId) {
        final FormMappingService formMappingService = getTenantAccessor().getFormMappingService();

        return null;
    }

    @Override
    public FormMapping getProcessOverviewForm(final long processDefinitionId) {
        return null;
    }

    @Override
    public FormMapping getTaskForm(final long processDefinitionId, final String taskName) {
        return null;
    }

    @Override
    public void updateFormMapping(final long formMappingId, final String page, final FormMappingType type, final boolean external) {

    }
}
