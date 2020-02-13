/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.services;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hibernate.cfg.Configuration;

/**
 * @author Baptiste Mesta
 */
public enum Vendor {

    ORACLE, SQLSERVER, POSTGRES, MYSQL, OTHER;

    /**
     * Get database vendor from databases metadata
     */
    public static Vendor fromDatabaseMetadata(DatabaseMetaData metadata) throws SQLException {
        if (metadata != null) {
            String productName = metadata.getDatabaseProductName();
            if (containsIgnoreCase(productName, "Oracle")) {
                return ORACLE;
            }
            if (containsIgnoreCase(productName, "Microsoft SQL Server")) {
                return SQLSERVER;
            }
        }
        return OTHER;
    }

    /**
     * Get database vendor from databases metadata
     */
    public static Vendor fromHibernateConfiguration(Configuration configuration) {
        String hibernateDialect = configuration.getProperty("hibernate.dialect");
        return fromHibernateDialectProperty(hibernateDialect);
    }

    public static Vendor fromHibernateDialectProperty(String hibernateDialect) {
        if (hibernateDialect != null) {
            if (hibernateDialect.toLowerCase().contains("postgresql")) {
                return POSTGRES;
            } else if (hibernateDialect.toLowerCase().contains("sqlserver")) {
                return SQLSERVER;
            } else if (hibernateDialect.toLowerCase().contains("oracle")) {
                return ORACLE;
            } else if (hibernateDialect.toLowerCase().contains("mysql")) {
                return MYSQL;
            }
        }
        return OTHER;
    }
}
