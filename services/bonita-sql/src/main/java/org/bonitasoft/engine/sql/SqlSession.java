/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.StringUtil;

/**
 * @author Matthieu Chaffotte
 */
public class SqlSession {

    private final Connection connection;

    SqlSession(final Connection connection) {
        this.connection = connection;
    }

    public SqlResult select(final String query) throws SqlSessionException {
        try {
            final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            final ResultSet resultSet = statement.executeQuery(query);
            return new SqlResult(statement, resultSet);
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        }
    }

    public SqlResult select(final String query, final Object[] queryParameters) throws SqlSessionException {
        try {
            if (queryParameters == null || queryParameters.length == 0) {
                return select(query);
            }
            final PreparedStatement preparedstatement = connection.prepareStatement(query);
            for (int i = 0; i < queryParameters.length; i++) {
                preparedstatement.setObject(i + 1, queryParameters[i]);
            }
            final ResultSet resultSet = preparedstatement.executeQuery();
            return new SqlResult(preparedstatement, resultSet);
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        }
    }

    public void update(final String query) throws SqlSessionException {
        Statement statement = null;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.executeUpdate(query);
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (final SQLException e) {
                    throw new SqlSessionException(e);
                }
            }
        }
    }

    public <T> List<T> executeSelectWithConverters(final Class<T> clazz, final String query, Map<String, String> columnFieldMap,
            final Map<String, Converter> converters) throws SqlSessionException {
        SqlResult sqlResult = null;
        try {
            sqlResult = select(query);
            final ResultSet resultSet = sqlResult.getResultSet();
            if (columnFieldMap == null) {
                columnFieldMap = new HashMap<String, String>();
            }
            if (columnFieldMap.isEmpty()) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    final String column = resultSet.getMetaData().getColumnLabel(i);
                    columnFieldMap.put(column.toLowerCase(), column.toLowerCase());
                }
            }
            final List<T> list = new ArrayList<T>();
            final Converter defaultSqlConverter = new DefaultSqlConverter();
            while (resultSet.next()) {
                final T entity = clazz.newInstance();
                final int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    final String columnName = resultSet.getMetaData().getColumnName(i);

                    Converter converter = null;
                    if (converters == null || converters.isEmpty()) {
                        converter = defaultSqlConverter;
                    } else {
                        converter = converters.get(columnName);
                        if (converter == null) {
                            converter = defaultSqlConverter;
                        }
                    }
                    final Object object = converter.convert(resultSet, columnName);
                    final String fieldName = StringUtil.firstCharToUpperCase(columnFieldMap.get(columnName.toLowerCase()));
                    Method setMethod = null;
                    final Method[] methods = clazz.getMethods();
                    for (final Method method : methods) {
                        if (method.getName().equals("set" + fieldName)) {
                            setMethod = method;
                            break;
                        }
                    }
                    if (setMethod != null) {
                        setMethod.invoke(entity, object);
                    }
                }
                list.add(entity);
            }
            return list;
        } catch (final SqlSessionException e) {
            throw e;
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        } catch (final InstantiationException e) {
            throw new SqlSessionException(e);
        } catch (final IllegalAccessException e) {
            throw new SqlSessionException(e);
        } catch (final IllegalArgumentException e) {
            throw new SqlSessionException(e);
        } catch (final InvocationTargetException e) {
            throw new SqlSessionException(e);
        } catch (final SecurityException e) {
            throw new SqlSessionException(e);
        } finally {
            if (sqlResult != null) {
                sqlResult.close();
            }
        }
    }

    public <T> List<T> executeSelect(final Class<T> clazz, final String query, final Map<String, String> columnFieldMap,
            final Map<String, Class<?>> sqlTypeJavaClassMap) throws SqlSessionException {
        final Converter converter = new ColumnTypeConverter(sqlTypeJavaClassMap);
        final Map<String, Converter> converters = new HashMap<String, Converter>();
        if (columnFieldMap != null) {
            for (final String columnName : columnFieldMap.keySet()) {
                converters.put(columnName, converter);
            }
        }
        return executeSelectWithConverters(clazz, query, columnFieldMap, converters);
    }

    public void disconnect() throws SqlSessionException {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        }
    }

}
