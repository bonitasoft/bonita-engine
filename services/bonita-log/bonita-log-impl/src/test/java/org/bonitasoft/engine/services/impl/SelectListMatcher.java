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
package org.bonitasoft.engine.services.impl;

import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.mockito.ArgumentMatcher;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SelectListMatcher extends ArgumentMatcher<SelectListDescriptor<?>> {
    

    private int startIndex;
    private int maxResults;
    private String orderByField;
    private OrderByType orderByType;
    private String queryName;

    public SelectListMatcher(String queryName, int startIndex, int maxResults, String orderByField, OrderByType orderByType) {
        this.queryName = queryName;
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.orderByField = orderByField;
        this.orderByType = orderByType;
    }
    
    @Override
    public boolean matches(Object argument) {
        if(!(argument instanceof SelectListDescriptor<?>)) {
            return false;
        }
        SelectListDescriptor<?> selectDescr = (SelectListDescriptor<?>) argument;
        QueryOptions queryOptions = selectDescr.getQueryOptions();
        OrderByOption orderBy = queryOptions.getOrderByOptions().get(0);
        return queryName.equals(selectDescr.getQueryName())
                && startIndex == selectDescr.getStartIndex()
                && maxResults == selectDescr.getPageSize()
                && orderByField.equals(orderBy.getFieldName())
                && orderByType.equals(orderBy.getOrderByType());
    }

}
