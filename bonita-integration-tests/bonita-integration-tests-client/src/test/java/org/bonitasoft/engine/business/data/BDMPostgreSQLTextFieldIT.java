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
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.sql.DataSource;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test to verify that TEXT fields in BDM are correctly mapped to PostgreSQL TEXT columns
 * instead of OID columns (which was the default behavior in Hibernate 5.6.2+).
 * This test validates the fix for the PostgreSQL TEXT field handling in EntityCodeGenerator
 * which uses @Type(type="org.bonitasoft.engine.persistence.PostgresMaterializedClobType) annotation
 * to force TEXT column type.
 */
public class BDMPostgreSQLTextFieldIT extends CommonAPIIT {

    private static final String BDM_PACKAGE_PREFIX = "com.company.model";
    private static final String DOCUMENT_BO = "Document";

    @Before
    public void setUp() throws Exception {
        loginWithTechnicalUser();

        // Only run this test on PostgreSQL
        String dbVendor = System.getProperty("sysprop.bonita.bdm.db.vendor", "h2");
        assumeTrue("This test only runs on PostgreSQL", dbVendor.toLowerCase().contains("postgres"));
    }

    // No @After needed: CommonAPIIT.clean() handles BDM and logout

    @Test
    public void should_create_TEXT_column_for_TEXT_fields_in_postgresql() throws Exception {
        // given: a BDM with a TEXT field
        final BusinessObject documentBO = new BusinessObject();
        documentBO.setQualifiedName(BDM_PACKAGE_PREFIX + "." + DOCUMENT_BO);

        final SimpleField contentField = new SimpleField();
        contentField.setName("content");
        contentField.setType(FieldType.TEXT);
        contentField.setNullable(true);
        documentBO.addField(contentField);

        final SimpleField descriptionField = new SimpleField();
        descriptionField.setName("description");
        descriptionField.setType(FieldType.TEXT);
        descriptionField.setNullable(false);
        documentBO.addField(descriptionField);

        final BusinessObjectModel businessObjectModel = new BusinessObjectModel();
        businessObjectModel.addBusinessObject(documentBO);

        // when: deploy the BDM
        final byte[] zip = new BusinessObjectModelConverter().zip(businessObjectModel);
        final String businessDataModelVersion = installBusinessDataModel(zip);

        assertThat(businessDataModelVersion).as("should have deployed BDM").isNotNull();

        // then: verify column types in PostgreSQL database
        verifyColumnType("document", "content", "text");
        verifyColumnType("document", "description", "text");
    }

    private void verifyColumnType(String tableName, String columnName, String expectedType) throws Exception {
        // Get BDM datasource from JNDI
        Context initialContext = new javax.naming.InitialContext();
        DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/NotManagedBizDataDS");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Query column information from PostgreSQL
            try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
                assertThat(rs.next())
                        .as("Column " + columnName + " should exist in table " + tableName)
                        .isTrue();

                String typeName = rs.getString("TYPE_NAME");
                assertThat(typeName.toLowerCase())
                        .as("Column " + columnName + " should be of type TEXT, not OID")
                        .isEqualTo(expectedType);
            }
        }
    }
}
