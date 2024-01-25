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
package org.bonitasoft.engine.persistence;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

/**
 * @author Baptiste Mesta
 */
@Slf4j
public class QueryBuilderFactory {

    private OrderByCheckingMode orderByCheckingMode;
    private OrderByBuilder orderByBuilder = new DefaultOrderByBuilder();
    private Map<String, String> classAliasMappings;
    private char likeEscapeCharacter;

    public QueryBuilderFactory(OrderByCheckingMode orderByCheckingMode, Map<String, String> classAliasMappings,
            char likeEscapeCharacter)
            throws Exception {
        this.orderByCheckingMode = orderByCheckingMode;
        this.classAliasMappings = classAliasMappings;
        this.likeEscapeCharacter = likeEscapeCharacter;
    }

    public <T> QueryBuilder createQueryBuilderFor(Session session,
            SelectListDescriptor<T> selectDescriptor) {
        Query query = session.getNamedQuery(selectDescriptor.getQueryName());
        if (query instanceof NativeQuery) {
            return new SQLQueryBuilder<>(session, query, orderByBuilder, classAliasMappings,
                    likeEscapeCharacter,
                    orderByCheckingMode, selectDescriptor);
        } else {
            return new HQLQueryBuilder<>(session, query, orderByBuilder, classAliasMappings, likeEscapeCharacter,
                    orderByCheckingMode, selectDescriptor);
        }
    }

    public void setOrderByBuilder(OrderByBuilder orderByBuilder) {
        this.orderByBuilder = orderByBuilder;
    }
}
