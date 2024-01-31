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
package org.bonitasoft.web.rest.server.datastore.filter;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.web.rest.server.datastore.converter.ValueConverter;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;

/**
 * @author Vincent Elcrin
 */
public class FilterAccessor {

    private final Map<String, String> filters;

    public FilterAccessor(Map<String, String> filters) {
        this.filters = filters;
    }

    /**
     * @throws APIFilterMandatoryException
     *         if the value of the filter is null
     */
    public String getMandatory(String filter) {
        ensureFilterValue(filter);
        return getFilters().get(filter);
    }

    /**
     * @throws APIFilterMandatoryException
     *         In case of any exception during conversion
     */
    public <S extends Serializable> S getMandatory(String filter, ValueConverter<S> converter) {
        String value = getMandatory(filter);
        try {
            return converter.convert(value);
        } catch (Exception e) {
            throw new APIFilterMandatoryException(filter, e);
        }
    }

    private Map<String, String> getFilters() {
        return filters;
    }

    private void ensureFilterValue(String filter) {
        if (getFilters() == null || !getFilters().containsKey(filter)) {
            throw new APIFilterMandatoryException(filter);
        }
    }
}
