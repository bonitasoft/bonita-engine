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
import org.bonitasoft.engine.service.*;

/**
 * @author Baptiste Mesta
 */
public class ProcessConfigurationAPIImpl {

    protected ServiceAccessor getServiceAccessor() {
        try {
            return ServiceAccessorSingleton.getInstance();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public SearchResult<FormMapping> searchFormMappings(final SearchOptions searchOptions) throws SearchException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        FormMappingService formMappingService = serviceAccessor.getFormMappingService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchFormMappings searchFormMappings = new SearchFormMappings(formMappingService,
                getServiceAccessor().getProcessDefinitionService(),
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
        final FormMappingService formMappingService = getServiceAccessor().getFormMappingService();
        try {
            return ModelConvertor.toFormMapping(formMappingService.get(formMappingId),
                    new FormRequiredAnalyzer(getServiceAccessor()
                            .getProcessDefinitionService()));
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (SObjectNotFoundException e) {
            throw new FormMappingNotFoundException("no form mapping found with id" + formMappingId);
        }
    }
}
