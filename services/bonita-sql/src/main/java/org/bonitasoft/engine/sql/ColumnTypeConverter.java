/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ColumnTypeConverter implements Converter {

    private final Map<String, Class<?>> sqlTypeJavaClassMap;

    public ColumnTypeConverter(final Map<String, Class<?>> sqlTypeJavaClassMap) {
        super();
        this.sqlTypeJavaClassMap = sqlTypeJavaClassMap;
    }

    @Override
    public Object convert(final ResultSet resultSet, final String columnName) throws SqlSessionException {
        try {
            if (sqlTypeJavaClassMap != null) {
                try {
                    return resultSet.getObject(columnName, sqlTypeJavaClassMap);
                } catch (final SQLException e) {
                    return resultSet.getObject(columnName);
                }
            } else {
                return resultSet.getObject(columnName);
            }
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        }
    }

}
