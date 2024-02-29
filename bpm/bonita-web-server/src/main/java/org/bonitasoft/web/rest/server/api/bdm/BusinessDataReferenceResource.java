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

import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataReferenceResource extends CommonResource {

    private final BusinessDataAPI bdmAPI;

    public BusinessDataReferenceResource(final BusinessDataAPI bdmAPI) {
        this.bdmAPI = bdmAPI;
    }

    @Get("json")
    public BusinessDataReferenceClient getProcessBusinessDataReference() throws DataNotFoundException {
        final String businessDataName = getPathParam("dataName");
        final Long processInstanceId = getPathParamAsLong("caseId");
        return toClient(bdmAPI.getProcessBusinessDataReference(businessDataName, processInstanceId));
    }

    public static BusinessDataReferenceClient toClient(BusinessDataReference object) {
        if (object instanceof SimpleBusinessDataReference) {
            final SimpleBusinessDataReference businessDataReference = (SimpleBusinessDataReference) object;
            return new SimpleBusinessDataReferenceClient(object.getName(), object.getType(),
                    getUrl(object.getType(), getStorageIdString(businessDataReference)),
                    businessDataReference.getStorageId());
        } else {
            final MultipleBusinessDataReference businessDataReference = (MultipleBusinessDataReference) object;
            return new MultipleBusinessDataReferenceClient(object.getName(), object.getType(),
                    getUrl(businessDataReference.getType(), getValue(businessDataReference)),
                    businessDataReference.getStorageIds());
        }
    }

    private static String getStorageIdString(SimpleBusinessDataReference businessDataReference) {
        Long storageId = businessDataReference.getStorageId();
        if (storageId != null) {
            return storageId.toString();
        }
        return "";
    }

    static String getValue(MultipleBusinessDataReference multipleBusinessDataReference) {
        return "findByIds?ids=" + multipleBusinessDataReference.getStorageIds().toString().replaceAll("[\\[\\] ]", "");
    }

    private static String getUrl(String type, String value) {
        return "API" + BonitaRestletApplication.BDM_BUSINESS_DATA_URL + "/" + type + "/" + value;
    }

    @Override
    protected void doCatch(final Throwable throwable) {
        super.doCatch(throwable);
        if (throwable.getCause() instanceof DataNotFoundException) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

}
