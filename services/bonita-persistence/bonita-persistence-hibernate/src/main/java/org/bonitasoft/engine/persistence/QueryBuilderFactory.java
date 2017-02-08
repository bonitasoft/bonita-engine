/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import org.bonitasoft.engine.services.Vendor;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

/**
 * @author Baptiste Mesta
 */
public class QueryBuilderFactory {

    private Vendor vendor;

    public QueryBuilder createQueryBuilderFor(Query query, Class<? extends PersistentObject> entityType, OrderByBuilder orderByBuilder,
            Map<String, String> classAliasMappings,
            Map<String, Class<? extends PersistentObject>> interfaceToClassMapping, char likeEscapeCharacter) {
        if (query instanceof SQLQuery) {
            return new SQLQueryBuilder(query.getQueryString(), vendor, entityType, orderByBuilder, classAliasMappings, interfaceToClassMapping,
                    likeEscapeCharacter);
        } else {
            return new HQLQueryBuilder(query.getQueryString(), orderByBuilder, classAliasMappings, interfaceToClassMapping, likeEscapeCharacter);
        }
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

}
