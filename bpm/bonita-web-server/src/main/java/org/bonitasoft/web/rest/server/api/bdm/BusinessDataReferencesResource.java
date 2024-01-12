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
package org.bonitasoft.web.rest.server.api.bdm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.restlet.resource.Get;

/**
 * @author Matthieu Chaffotte
 * @author Colin Puy
 */
public class BusinessDataReferencesResource extends CommonResource {

    private final BusinessDataAPI bdmAPI;

    public BusinessDataReferencesResource(final BusinessDataAPI bdmAPI) {
        this.bdmAPI = bdmAPI;
    }

    @Get("json")
    public List<BusinessDataReference> getProcessBusinessDataReferences() {
        final Long processInstanceId = getCaseId();
        final Integer p = getSearchPageNumber();
        final Integer c = getSearchPageSize();
        return bdmAPI.getProcessBusinessDataReferences(processInstanceId, p * c, c);
    }

    private Long getCaseId() {
        final String[] values = getQuery().getValuesArray(APIServletCall.PARAMETER_FILTER);
        final Map<String, String> filters = parseFilters(Arrays.asList(values));
        final String caseId = filters.get("caseId");
        if (caseId == null) {
            throw new IllegalArgumentException("filter caseId is mandatory");
        }
        try {
            return Long.parseLong(caseId);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("filter caseId should be a number");
        }
    }

}
