/**
 * Copyright (C) 2026 Bonitasoft S.A.
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

import java.io.Serializable;

import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;

/**
 * Converts {@link BusinessDataReference} objects to their client representations.
 * Used by context controllers to transform execution context values before returning them to the client.
 */
public final class BusinessDataReferenceConverter {

    private static final String BDM_BUSINESS_DATA_URL = "/bdm/businessData";

    private BusinessDataReferenceConverter() {
    }

    /**
     * If the given object is a {@link BusinessDataReference}, converts it to a
     * {@link BusinessDataReferenceClient}. Otherwise, returns the object unchanged.
     */
    public static Serializable convertIfApplicable(Serializable object) {
        if (object instanceof BusinessDataReference reference) {
            return toClient(reference);
        }
        return object;
    }

    /**
     * Converts a {@link BusinessDataReference} to its client representation.
     */
    public static BusinessDataReferenceClient toClient(BusinessDataReference reference) {
        if (reference instanceof SimpleBusinessDataReference simpleReference) {
            return new SimpleBusinessDataReferenceClient(
                    reference.getName(),
                    reference.getType(),
                    getUrl(reference.getType(), getStorageIdString(simpleReference)),
                    simpleReference.getStorageId());
        } else {
            MultipleBusinessDataReference multipleReference = (MultipleBusinessDataReference) reference;
            return new MultipleBusinessDataReferenceClient(
                    reference.getName(),
                    reference.getType(),
                    getUrl(multipleReference.getType(), getStorageIdsValue(multipleReference)),
                    multipleReference.getStorageIds());
        }
    }

    private static String getStorageIdString(SimpleBusinessDataReference reference) {
        Long storageId = reference.getStorageId();
        if (storageId != null) {
            return storageId.toString();
        }
        return "";
    }

    private static String getStorageIdsValue(MultipleBusinessDataReference reference) {
        return "findByIds?ids=" + reference.getStorageIds().toString().replaceAll("[\\[\\] ]", "");
    }

    private static String getUrl(String type, String value) {
        return "API" + BDM_BUSINESS_DATA_URL + "/" + type + "/" + value;
    }
}
