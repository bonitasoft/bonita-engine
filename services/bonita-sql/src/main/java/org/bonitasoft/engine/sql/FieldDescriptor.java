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

public abstract class FieldDescriptor<T> {

    private final String columnName;

    private final String fieldName;

    private final Class<T> fieldType;

    public FieldDescriptor(final String columnName, final String fieldName, final Class<T> fieldType) {
        super();
        this.columnName = columnName;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    // column value object class is the one defined in the jdbc spec for the given
    // column type
    public abstract T convert(Class<T> clazz, Object columnValue) throws Exception;

    public String getColumnName() {
        return columnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class<T> getFieldType() {
        return fieldType;
    }

}
