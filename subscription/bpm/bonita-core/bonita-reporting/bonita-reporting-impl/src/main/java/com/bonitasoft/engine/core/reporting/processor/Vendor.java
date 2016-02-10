/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting.processor;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Databases Vendor
 * 
 * @author Colin PUY
 */
public enum Vendor {

    ORACLE, SQLSERVER, OTHER;

    /**
     * Get database vendor from databases metadatas
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
}
