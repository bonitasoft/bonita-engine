/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting.processor;

import static com.bonitasoft.engine.core.reporting.processor.Vendor.ORACLE;
import static com.bonitasoft.engine.core.reporting.processor.Vendor.OTHER;
import static com.bonitasoft.engine.core.reporting.processor.Vendor.SQLSERVER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.DatabaseMetaData;

import org.junit.Test;

public class VendorTest {

    @Test
    public void should_return_oracle_for_an_oracle_database() throws Exception {
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(metadata.getDatabaseProductName()).thenReturn("Oracle");

        Vendor vendor = Vendor.fromDatabaseMetadata(metadata);

        assertThat(vendor).isEqualTo(ORACLE);
    }

    @Test
    public void should_return_sqlserver_for_a_sql_server_database() throws Exception {
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(metadata.getDatabaseProductName()).thenReturn("Microsoft SQL Server");

        Vendor vendor = Vendor.fromDatabaseMetadata(metadata);

        assertThat(vendor).isEqualTo(SQLSERVER);
    }

    @Test
    public void should_return_other_for_any_other_database() throws Exception {
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(metadata.getDatabaseProductName()).thenReturn("MySql");

        Vendor vendor = Vendor.fromDatabaseMetadata(metadata);

        assertThat(vendor).isEqualTo(OTHER);

    }

    @Test
    public void should_return_other_for_any_other_cases() throws Exception {
        Vendor vendor = Vendor.fromDatabaseMetadata(null);

        assertThat(vendor).isEqualTo(OTHER);

    }
}
