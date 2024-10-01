/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.test.persistence.jdbc;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

/**
 * Spring JDBC RowMapper that converts column names and values as Hibernate woulds:
 * <ul>
 * <li>converts column names to upper case so that tests can assert on column name whatever the DB vendor</li>
 * <li>converts some column values to long, double, int or boolean if needed, notably on Oracle where types seem to be
 * harder to detect</li>
 * <li>converts Clob and Blob to String and byte[] respectively</li>
 * </ul>
 *
 * @author Emmanuel Duchastenier
 */
@Slf4j
public class JdbcRowMapper implements RowMapper<Map<String, Object>> {

    private final String dbVendor = System.getProperty("sysprop.bonita.db.vendor", "h2");

    private List<String> columnsToConvertToLong = new ArrayList<>(); // column names must be upper case
    private List<String> columnsToConvertToBoolean = new ArrayList<>(); // column names must be upper case
    private List<String> columnsToConvertToDouble = new ArrayList<>(); // column names must be upper case

    private final Map<String, JdbcValueConverter> converters = Map.of(
            "h2", new DefaultValueConverter(),
            "postgres", new DefaultValueConverter(),
            "mysql", new DefaultValueConverter(),
            "sqlserver", new DefaultValueConverter(),
            "oracle", new OracleValueConverter());

    public JdbcRowMapper() {
        log.info("Detected DB vendor: {}", dbVendor);
    }

    /**
     * @param columnsToConvertToLong column names must be upper case
     */
    public JdbcRowMapper(String... columnsToConvertToLong) {
        this();
        this.columnsToConvertToLong.addAll(List.of(columnsToConvertToLong));
    }

    /**
     * @param columnsToConvertToLong column names must be upper case
     * @param columnsToConvertToBoolean column names must be upper case
     */
    public JdbcRowMapper(List<String> columnsToConvertToLong, List<String> columnsToConvertToBoolean) {
        this();
        this.columnsToConvertToLong = columnsToConvertToLong;
        this.columnsToConvertToBoolean = columnsToConvertToBoolean;
    }

    public JdbcRowMapper(List<String> columnsToConvertToLong, List<String> columnsToConvertToBoolean,
            List<String> columnsToConvertToDouble) {
        this(columnsToConvertToLong, columnsToConvertToBoolean);
        this.columnsToConvertToDouble = columnsToConvertToDouble;
    }

    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            final Object object = rs.getObject(i);
            Object value = converters.get(dbVendor).getValue(rs, object, i);
            result.put(rs.getMetaData().getColumnName(i).toUpperCase(), value);
        }
        return result;
    }

    @FunctionalInterface
    public interface JdbcValueConverter {

        Object getValue(ResultSet resultSet, Object object, int rowNum) throws SQLException;
    }

    class DefaultValueConverter implements JdbcValueConverter {

        @Override
        public Object getValue(ResultSet rs, Object object, int columnNumber) throws SQLException {
            if (object == null) {
                return null;
            }

            if (object instanceof Clob) {
                final Clob clob = rs.getClob(columnNumber);
                return clob.getSubString(1, (int) clob.length());
            }
            if (object instanceof Blob) {
                final Blob blob = rs.getBlob(columnNumber);
                return blob.getBytes(1, (int) blob.length());
            }

            // if value must be converted to double, let's do it:
            final String upperCaseColumnName = rs.getMetaData().getColumnName(columnNumber).toUpperCase();
            if (columnsToConvertToDouble.contains(upperCaseColumnName)) {
                return ((Number) object).doubleValue();
            }
            // if value must be converted to long, let's do it:
            if (columnsToConvertToLong.contains(upperCaseColumnName)) {
                return ((Number) object).longValue();
            } else if (object instanceof Number) {
                // if value can be converted to int, let's do it:
                try {
                    return ((Number) object).intValue();
                } catch (Exception ignored) {
                }
            }
            return object;
        }
    }

    class OracleValueConverter extends DefaultValueConverter {

        @Override
        public Object getValue(ResultSet rs, Object object, int columnNumber) throws SQLException {
            if (object != null
                    && columnsToConvertToBoolean.contains(rs.getMetaData().getColumnName(columnNumber).toUpperCase())) {
                return "1".equals(rs.getString(columnNumber));
            } else {
                return super.getValue(rs, object, columnNumber);
            }
        }
    }

}
