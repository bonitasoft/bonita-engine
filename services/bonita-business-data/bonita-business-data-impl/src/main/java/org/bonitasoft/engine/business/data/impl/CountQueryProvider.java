/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Query;

/**
 * @author Elias Ricken de Medeiros
 */
public class CountQueryProvider {

    public Query getCountQueryDefinition(BusinessObject businessObject, Query baseQuery) {
        if (!baseQuery.hasMultipleResults()) {
            return null;
        }
        List<Query> queries = new ArrayList<>();
        queries.addAll(BDMQueryUtil.createCountProvidedQueriesForBusinessObject(businessObject));
        queries.addAll(businessObject.getQueries());
        return findRelatedCountQuery(baseQuery, queries);
    }

    private Query findRelatedCountQuery(Query baseQuery, List<Query> queryList) {
        Query countQuery = null;
        Iterator<Query> iterator = queryList.iterator();
        while (iterator.hasNext() && countQuery == null) {
            Query currentQuery = iterator.next();
            if (Long.class.getName().equals(currentQuery.getReturnType())
                    && currentQuery.getName().equals(BDMQueryUtil.getCountQueryName(baseQuery.getName()))) {
                countQuery = currentQuery;
            }
        }
        return countQuery;
    }

}
