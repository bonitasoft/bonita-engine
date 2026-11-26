/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.apache.tomcat.dbcp.dbcp2.managed.BasicManagedDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Builds the Bonita XA connection pool on the database of the test task (H2 for the {@code test} task, the vendor
 * database for the {@code <vendor>DatabaseTest} tasks) and enlists its connections in JTA transactions, as the engine
 * does. The validation on borrow must not leave a transaction open when the XA transaction starts.
 */
class BonitaDataSourceInitializerIT {

    /**
     * SQLState of the warning PostgreSQL raises when a {@code BEGIN} arrives inside an open transaction ("there is
     * already a transaction in progress"), the SQL standard "active SQL transaction" state.
     */
    private static final String ACTIVE_SQL_TRANSACTION = "25001";
    private static final int TRANSACTIONS = 3;

    private final TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager
            .transactionManager();
    private BasicManagedDataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor(System.getProperty("sysprop.bonita.db.vendor", "h2"))
                .url(System.getProperty("db.url", "jdbc:h2:mem:" + getClass().getSimpleName() + ";DB_CLOSE_DELAY=-1"))
                .user(System.getProperty("db.user", "sa"))
                .password(System.getProperty("db.password", ""))
                // a single connection: each transaction enlists the same connection, validated again on borrow
                .xaDatasource(DatasourceConfiguration.builder().maxPoolSize(1).build())
                .build();
        dataSource = new BonitaDataSourceInitializer().createManagedDataSource(configuration, transactionManager);
    }

    @AfterEach
    void tearDown() throws SQLException {
        dataSource.close();
    }

    @Test
    void createManagedDataSource_should_start_xa_branches_without_active_transaction_warning() throws Exception {
        //given
        List<SQLWarning> warnings = new ArrayList<>();

        //when
        for (int i = 0; i < TRANSACTIONS; i++) {
            warnings.addAll(borrowInTransaction());
        }

        //then
        assertThat(warnings).extracting(SQLWarning::getSQLState)
                .as("SQLStates of the warnings %s", warnings)
                .doesNotContain(ACTIVE_SQL_TRANSACTION);
    }

    /**
     * The test above can only fail on PostgreSQL. This one also runs on H2 with the unit tests, so that every build
     * fails if a validation query is put back on the XA pool, or if validation on borrow is turned off.
     */
    @Test
    void createManagedDataSource_should_validate_connections_on_borrow_without_validation_query() {
        //then: the pool built in setUp validates with Connection.isValid(), which leaves no transaction open
        assertThat(dataSource.getTestOnBorrow()).isTrue();
        assertThat(dataSource.getValidationQuery()).isNull();
    }

    /**
     * @return the warnings left on the connection by its validation and its enlistment in the transaction
     */
    private List<SQLWarning> borrowInTransaction() throws Exception {
        transactionManager.begin();
        try {
            List<SQLWarning> warnings = new ArrayList<>();
            // getConnection() borrows the connection (validated on borrow) and enlists it: XAResource.start
            try (Connection connection = dataSource.getConnection()) {
                for (SQLWarning warning = connection.getWarnings(); warning != null; warning = warning
                        .getNextWarning()) {
                    warnings.add(warning);
                }
            }
            transactionManager.commit();
            return warnings;
        } catch (Exception e) {
            if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                transactionManager.rollback();
            }
            throw e;
        }
    }
}
