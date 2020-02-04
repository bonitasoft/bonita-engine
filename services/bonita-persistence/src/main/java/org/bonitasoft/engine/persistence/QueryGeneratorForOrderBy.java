/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import java.util.List;
import java.util.Map;

public class QueryGeneratorForOrderBy {

    private Map<String, String> classAliasMappings;
    private OrderByBuilder orderByBuilder;

    public QueryGeneratorForOrderBy(Map<String, String> classAliasMappings, OrderByBuilder orderByBuilder) {

        this.classAliasMappings = classAliasMappings;
        this.orderByBuilder = orderByBuilder;
    }

    void appendClassAlias(final StringBuilder builder, final Class<? extends PersistentObject> clazz)
            throws SBonitaReadException {
        final String className = clazz.getName();
        final String classAlias = classAliasMappings.get(className);
        if (classAlias == null || classAlias.trim().isEmpty()) {
            throw new SBonitaReadException("No class alias found for class " + className);
        }
        builder.append(classAlias);
        builder.append('.');
    }

    String generate(List<OrderByOption> orderByOptions, Class<? extends PersistentObject> entityType)
            throws SBonitaReadException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ORDER BY ");
        boolean startWithComma = false;
        boolean sortedById = false;
        for (final OrderByOption orderByOption : orderByOptions) {
            if (startWithComma) {
                stringBuilder.append(',');
            }
            StringBuilder fieldNameBuilder = new StringBuilder();
            final Class<? extends PersistentObject> clazz = orderByOption.getClazz();
            if (clazz != null) {
                appendClassAlias(fieldNameBuilder, clazz);
            }
            final String fieldName = orderByOption.getFieldName();
            if ("id".equalsIgnoreCase(fieldName) || "sourceObjectId".equalsIgnoreCase(fieldName)) {
                sortedById = true;
            }
            fieldNameBuilder.append(fieldName);
            orderByBuilder.appendOrderBy(stringBuilder, fieldNameBuilder.toString(),
                    orderByOption.getOrderByType());
            startWithComma = true;
        }
        if (!sortedById) {
            if (startWithComma) {
                stringBuilder.append(',');
            }
            appendClassAlias(stringBuilder, entityType);
            stringBuilder.append("id");
            stringBuilder.append(' ');
            stringBuilder.append("ASC");
        }
        return stringBuilder.toString();
    }
}
