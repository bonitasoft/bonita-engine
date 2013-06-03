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
import java.util.Map;
import java.util.Map.Entry;

public class DefaultFieldSetter implements FieldSetter {

    private Map<String, Converter> columnConverterMap = null;

    private String column = null;

    public DefaultFieldSetter(final String column) {
        this.column = column;
    }

    /**
     * By default, just one entry (the pair of column and converter) in the
     * columnConverterMap
     * 
     * @param columnConverterMap
     */
    public DefaultFieldSetter(final Map<String, Converter> columnConverterMap) {
        this.columnConverterMap = columnConverterMap;
    }

    @Override
    public Object setField(final ResultSet resultSet) throws SqlSessionException {
        Object obj = null;
        final Converter defaultSqlConverter = new DefaultSqlConverter();
        if (columnConverterMap != null) {
            for (final Entry<String, Converter> entry : columnConverterMap.entrySet()) {
                Converter converter = entry.getValue();
                if (converter == null) {
                    converter = defaultSqlConverter;
                }
                obj = converter.convert(resultSet, entry.getKey());
                break;
            }
        } else {
            obj = defaultSqlConverter.convert(resultSet, column);
        }
        return obj;
    }

}
