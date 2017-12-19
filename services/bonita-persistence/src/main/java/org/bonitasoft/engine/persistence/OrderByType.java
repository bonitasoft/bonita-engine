/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

/**
 * @author Charles Souillard
 */
public enum OrderByType {

    ASC, DESC, ASC_NULLS_LAST, DESC_NULLS_FIRST, ASC_NULLS_FIRST, DESC_NULLS_LAST;

    private String sqlKeyword;

    static {
        ASC.sqlKeyword = "ASC";
        DESC.sqlKeyword = "DESC";
        ASC_NULLS_LAST.sqlKeyword = "ASC NULLS LAST";
        DESC_NULLS_FIRST.sqlKeyword = "DESC NULLS FIRST";
        ASC_NULLS_FIRST.sqlKeyword = "ASC NULLS FIRST";
        DESC_NULLS_LAST.sqlKeyword = "DESC NULLS LAST";

    }

    public String getSqlKeyword() {
        return sqlKeyword;
    }

}
