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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.LongType;

/**
 * @author Baptiste Mesta
 */
public class SQLQueryBuilder extends QueryBuilder {

    private static Map<String, String> hqlToSqlAlias = new HashMap<>();

    static {
        hqlToSqlAlias.put("user", "user_");
    }

    private Class<? extends PersistentObject> entityType;

    SQLQueryBuilder(String baseQuery, Class<? extends PersistentObject> entityType, OrderByBuilder orderByBuilder, Map<String, String> classAliasMappings,
            Map<String, Class<? extends PersistentObject>> interfaceToClassMapping, char likeEscapeCharacter) {
        super(baseQuery, orderByBuilder, classAliasMappings, interfaceToClassMapping, likeEscapeCharacter);
        this.entityType = entityType;
    }

    Query buildQuery(Session session) {
        String builtQuery = stringQueryBuilder.toString();
        builtQuery = replaceHQLAliasesBySQLAliases(builtQuery);
        SQLQuery sqlQuery = session.createSQLQuery(builtQuery);
        setReturnType(builtQuery, sqlQuery);
        return sqlQuery;
    }

    private void setReturnType(String builtQuery, SQLQuery sqlQuery) {
        if (isCountQuery(builtQuery)) {
            sqlQuery.addScalar("count", LongType.INSTANCE);
        } else {
            String hqlAlias = classAliasMappings.get(entityType.getName());
            String sqlAlias = hqlToSqlAlias.containsKey(hqlAlias) ? hqlAlias.replace("user", "user_") : hqlAlias;
            Class<? extends PersistentObject> entityClass = interfaceToClassMapping.get(entityType.getName());
            sqlQuery.addEntity(sqlAlias, entityClass.getName());
        }
    }

    private boolean isCountQuery(String builtQuery) {
        return builtQuery.contains("count(");
    }

    private String replaceHQLAliasesBySQLAliases(String builtQuery) {
        for (String aliasToReplace : hqlToSqlAlias.keySet()) {
            if (builtQuery.contains(aliasToReplace + ".")) {
                builtQuery = builtQuery.replace(aliasToReplace + ".", hqlToSqlAlias.get(aliasToReplace) + ".");
            }
        }
        return builtQuery;
    }

    @Override
    public void setTenantId(Query query, long tenantId) {
        if (query.getQueryString().contains(":tenantId")) {
            query.setParameter("tenantId", tenantId);
        }
    }
}
