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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.services.Vendor;
import org.hibernate.Session;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryScalarReturn;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

/**
 * @author Baptiste Mesta
 */
public class SQLQueryBuilder<T> extends QueryBuilder<T> {

    public static final String TRUE_VALUE_PARAMETER = "trueValue";
    private static Map<String, String> hqlToSqlAlias = new HashMap<>();

    static {
        hqlToSqlAlias.put("user", "user_");
    }

    private final Vendor vendor;

    SQLQueryBuilder(Session session, Query baseQuery, Vendor vendor,
            OrderByBuilder orderByBuilder,
            Map<String, String> classAliasMappings, char likeEscapeCharacter,
            boolean wordSearchEnabled,
            OrderByCheckingMode orderByCheckingMode,
            SelectListDescriptor<T> selectDescriptor) {
        super(session, baseQuery, orderByBuilder, classAliasMappings, likeEscapeCharacter, wordSearchEnabled,
                orderByCheckingMode,
                selectDescriptor, useIntegerForBoolean(vendor));
        this.vendor = vendor;
    }

    public void addConstantsAsParameters(Query sqlQuery) {
        if (sqlQuery.getQueryString().contains(":" + TRUE_VALUE_PARAMETER)) {
            if (useIntegerForBoolean(vendor)) {
                sqlQuery.setParameter(TRUE_VALUE_PARAMETER, 1);
            } else {
                sqlQuery.setParameter(TRUE_VALUE_PARAMETER, true);
            }
        }
    }

    private String replaceHQLAliasesBySQLAliases(String builtQuery) {
        for (Map.Entry<String, String> aliasToReplace : hqlToSqlAlias.entrySet()) {
            if (builtQuery.contains(aliasToReplace.getKey() + ".")) {
                builtQuery = builtQuery.replace(aliasToReplace.getKey() + ".", aliasToReplace.getValue() + ".");
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

    private static boolean useIntegerForBoolean(Vendor vendor) {
        return Vendor.ORACLE.equals(vendor) || Vendor.SQLSERVER.equals(vendor);
    }

    @Override
    Query rebuildQuery(AbstractSelectDescriptor<T> selectDescriptor, Session session, Query query) {
        String builtQuery = stringQueryBuilder.toString();
        builtQuery = replaceHQLAliasesBySQLAliases(builtQuery);
        NativeQuery generatedSqlQuery = session.createSQLQuery(builtQuery);
        for (NativeSQLQueryReturn queryReturn : (List<NativeSQLQueryReturn>) ((NativeQuery) query)
                .getQueryReturns()) {
            if (queryReturn instanceof NativeSQLQueryScalarReturn) {
                generatedSqlQuery.addScalar(((NativeSQLQueryScalarReturn) queryReturn).getColumnAlias(),
                        ((NativeSQLQueryScalarReturn) queryReturn).getType());
            } else if (queryReturn instanceof NativeSQLQueryRootReturn) {
                generatedSqlQuery.addEntity(((NativeSQLQueryRootReturn) queryReturn).getAlias(),
                        ((NativeSQLQueryRootReturn) queryReturn).getReturnEntityName());
            } else {
                throw new IllegalStateException(
                        "Not yet implemented. Query return type " + queryReturn.getClass().getName());
            }
        }
        return generatedSqlQuery;
    }
}
