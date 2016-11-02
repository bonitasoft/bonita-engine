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

/**
 * @author Baptiste Mesta
 */
public class SQLServerOrderByBuilder implements OrderByBuilder {

    @Override
    public void appendOrderBy(StringBuilder builder, String fieldName, OrderByType orderByType) {
        switch (orderByType) {
            case ASC:
            case DESC:
                builder.append(fieldName).append(" ").append(orderByType.getSqlKeyword());
                break;
            case ASC_NULLS_FIRST:
                builder.append("CASE WHEN ").append(fieldName).append(" IS NULL THEN 0 ELSE 1 END ASC, ")
                        .append(fieldName).append(" ASC");
                break;
            case ASC_NULLS_LAST:
                builder.append("CASE WHEN ").append(fieldName).append(" IS NULL THEN 0 ELSE 1 END DESC, ")
                        .append(fieldName).append(" ASC");
                break;
            case DESC_NULLS_FIRST:
                builder.append("CASE WHEN ").append(fieldName).append(" IS NULL THEN 0 ELSE 1 END ASC, ")
                        .append(fieldName).append(" DESC");
                break;
            case DESC_NULLS_LAST:
                builder.append("CASE WHEN ").append(fieldName).append(" IS NULL THEN 0 ELSE 1 END DESC, ")
                        .append(fieldName).append(" DESC");
                break;
        }
    }
}
