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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static java.lang.String.format;

import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.cases.CaseVariableItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;

/**
 * @author Colin PUY
 */
public class APICaseVariableAttributeChecker {

    public void checkUpdateAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new APIException(format("Attributes '%s' and '%s' must be specified", CaseVariableItem.ATTRIBUTE_TYPE,
                    CaseVariableItem.ATTRIBUTE_VALUE));
        }
        if (!attributes.containsKey(CaseVariableItem.ATTRIBUTE_TYPE)) {
            throw new APIException(format("Attribute '%s' must be specified", CaseVariableItem.ATTRIBUTE_TYPE));
        }
        if (!attributes.containsKey(CaseVariableItem.ATTRIBUTE_VALUE)) {
            throw new APIException(format("Attribute '%s' must be specified", CaseVariableItem.ATTRIBUTE_VALUE));
        }
    }

    // filters could be null...
    public void checkSearchFilters(Map<String, String> filters) {
        if (filters == null || !filters.containsKey(CaseVariableItem.ATTRIBUTE_CASE_ID)) {
            throw new APIException(format("Filter '%s' must be specified", CaseVariableItem.ATTRIBUTE_CASE_ID));
        }
    }
}
