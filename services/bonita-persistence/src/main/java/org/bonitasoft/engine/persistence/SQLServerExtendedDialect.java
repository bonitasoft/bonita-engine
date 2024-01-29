/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

import java.sql.Types;

import org.hibernate.dialect.SQLServer2012Dialect;

/**
 * @author Emmanuel Duchastenier
 */
public class SQLServerExtendedDialect extends SQLServer2012Dialect {

    public SQLServerExtendedDialect() {
        super();
        registerColumnType(Types.CHAR, "nchar(1)");
        registerColumnType(Types.VARCHAR, "nvarchar($l)");
        registerColumnType(Types.VARCHAR, 8000, "nvarchar($l)");
        registerColumnType(Types.LONGVARCHAR, "nvarchar($l)");
        registerColumnType(Types.CLOB, "ntext");
    }

}
