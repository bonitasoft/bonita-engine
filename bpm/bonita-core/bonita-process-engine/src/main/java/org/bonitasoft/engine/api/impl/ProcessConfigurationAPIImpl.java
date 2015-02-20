/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.api.impl;

import java.util.List;

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
    public SearchResult<FormMapping> searchFormMappings(SearchOptions searchOptions) {
        return null;
    }

    @Override
    public FormMapping getProcessStartForm(long processDefinitionId) {
        FormMappingService formMappingService = getTenantAccessor().getFormMappingService();
        
        return null;
    }

    @Override
    public FormMapping getProcessOverviewForm(long processDefinitionId) {
        return null;
    }

    @Override
    public FormMapping getHumanTaskForm(long processDefinitionId, String taskName) {
        return null;
    }

    @Override
    public void updateFormMapping(long formMappingId, String page, FormMappingType type, boolean external) {

    }
}
