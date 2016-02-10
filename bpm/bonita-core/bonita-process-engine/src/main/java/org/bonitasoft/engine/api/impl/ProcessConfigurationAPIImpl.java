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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.FormMappingNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.form.SearchFormMappings;
import org.bonitasoft.engine.service.FormRequiredAnalyzer;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Baptiste Mesta
 */
public class ProcessConfigurationAPIImpl {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public SearchResult<FormMapping> searchFormMappings(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        FormMappingService formMappingService = tenantAccessor.getFormMappingService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchFormMappings searchFormMappings = new SearchFormMappings(formMappingService, getTenantAccessor().getProcessDefinitionService(),
                searchEntitiesDescriptor.getSearchFormMappingDescriptor(),
                searchOptions);
        try {
            searchFormMappings.execute();
            return searchFormMappings.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    public FormMapping getFormMapping(long formMappingId) throws FormMappingNotFoundException {
        final FormMappingService formMappingService = getTenantAccessor().getFormMappingService();
        try {
            return ModelConvertor.toFormMapping(formMappingService.get(formMappingId), new FormRequiredAnalyzer(getTenantAccessor()
                    .getProcessDefinitionService()));
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (SObjectNotFoundException e) {
            throw new FormMappingNotFoundException("no form mapping found with id" + formMappingId);
        }
    }
}
