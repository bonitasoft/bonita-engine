/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.persistence.dialect;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * @author Matthieu Chaffotte
 */
public class SQLServer2008Dialect extends SQLServerDialect {

    private static final int MAX_LENGTH = 8000;

    public SQLServer2008Dialect() {
        registerColumnType(Types.BLOB, "varbinary(MAX)");
        registerColumnType(Types.VARBINARY, "varbinary(MAX)");
        registerColumnType(Types.VARBINARY, MAX_LENGTH, "varbinary($l)");
        registerColumnType(Types.LONGVARBINARY, "varbinary(MAX)");

        registerColumnType(Types.CLOB, "varchar(MAX)");
        registerColumnType(Types.LONGVARCHAR, "varchar(MAX)");
        registerColumnType(Types.VARCHAR, "varchar(MAX)");
        registerColumnType(Types.VARCHAR, MAX_LENGTH, "varchar($l)");

        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.BIT, "bit");
        registerColumnType(Types.BOOLEAN, "bit");

        registerFunction("row_number", new NoArgSQLFunction("row_number", StandardBasicTypes.INTEGER, true));

        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "datetime2");

        registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", Hibernate.TIMESTAMP, false));
    }

}
