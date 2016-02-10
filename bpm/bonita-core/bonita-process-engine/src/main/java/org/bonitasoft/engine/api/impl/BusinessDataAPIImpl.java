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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.converter.BusinessDataModelConverter;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataAPIImpl implements BusinessDataAPI {

    protected TenantServiceAccessor getTenantAccessor() {
        return APIUtils.getTenantAccessor();
    }

    @Override
    public BusinessDataReference getProcessBusinessDataReference(final String businessDataName, final long processInstanceId) throws DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final RefBusinessDataService refBusinessDataService = tenantAccessor.getRefBusinessDataService();
            final SRefBusinessDataInstance sReference = refBusinessDataService.getRefBusinessDataInstance(businessDataName,
                    processInstanceId);
            if (sReference instanceof SSimpleRefBusinessDataInstance) {
                return BusinessDataModelConverter.toSimpleBusinessDataReference((SSimpleRefBusinessDataInstance) sReference);
            } else {
                return BusinessDataModelConverter.toMultipleBusinessDataReference((SMultiRefBusinessDataInstance) sReference);
            }
        } catch (final SRefBusinessDataInstanceNotFoundException srbdnfe) {
            throw new DataNotFoundException(srbdnfe);
        } catch (final SBonitaReadException sbre) {
            throw new RetrieveException(sbre);
        }
    }

    @Override
    public List<BusinessDataReference> getProcessBusinessDataReferences(final long processInstanceId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final RefBusinessDataService refBusinessDataService = tenantAccessor.getRefBusinessDataService();
            final List<SRefBusinessDataInstance> sReferences = refBusinessDataService.getRefBusinessDataInstances(processInstanceId, startIndex, maxResults);
            final List<BusinessDataReference> references = new ArrayList<BusinessDataReference>();
            for (final SRefBusinessDataInstance sReference : sReferences) {
                if (sReference instanceof SSimpleRefBusinessDataInstance) {
                    references.add(BusinessDataModelConverter.toSimpleBusinessDataReference((SSimpleRefBusinessDataInstance) sReference));
                } else {
                    references.add(BusinessDataModelConverter.toMultipleBusinessDataReference((SMultiRefBusinessDataInstance) sReference));
                }
            }
            return references;
        } catch (final SBonitaReadException sbre) {
            throw new RetrieveException(sbre);
        }
    }

}
