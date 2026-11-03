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
package org.bonitasoft.platform.configuration.impl;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    // the spied methods never touch the collaborators, so null JdbcTemplate/TransactionTemplate are fine
    @Spy
    ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(null, null, "h2");

    @Test
    public void getPlatformPortalConf_should_call_query_for_PLATFORM_PORTAL_type() {
        Mockito.doReturn(Collections.EMPTY_LIST).when(configurationService)
                .getNonTenantResource(ArgumentMatchers.any(ConfigurationType.class));

        configurationService.getPlatformPortalConf();

        Mockito.verify(configurationService).getNonTenantResource(ConfigurationType.PLATFORM_PORTAL);
    }

    @Test
    public void getPlatformEngineConf_should_call_query_for_PLATFORM_ENGINE_type() {
        Mockito.doReturn(Collections.EMPTY_LIST).when(configurationService)
                .getNonTenantResource(ArgumentMatchers.any(ConfigurationType.class));

        configurationService.getPlatformEngineConf();

        Mockito.verify(configurationService).getNonTenantResource(ConfigurationType.PLATFORM_ENGINE);
    }

    @Test
    public void getLicenses_should_call_query_for_LICENSES_type() throws Exception {
        Mockito.doReturn(Collections.EMPTY_LIST).when(configurationService)
                .getNonTenantResource(ArgumentMatchers.any(ConfigurationType.class));

        configurationService.getLicenses();

        Mockito.verify(configurationService).getNonTenantResource(ConfigurationType.LICENSES);
    }

    @Test
    public void should_write_file_within_sub_folder() throws Exception {
        //given
        final File configFolder = temporaryFolder.newFolder("conf");
        final File licFolder = temporaryFolder.newFolder("lic");
        List<FullBonitaConfiguration> confs = new ArrayList<>();
        confs.add(new FullBonitaConfiguration("conf1.properties", "content 1".getBytes(), "PLATFORM_TYPE", 0L));
        confs.add(new FullBonitaConfiguration("conf2.properties", "content 2".getBytes(), "TENANT_TYPE", 5L));

        Mockito.doReturn(confs).when(configurationService).getAllConfiguration();
        Mockito.doCallRealMethod().when(configurationService).writeAllConfigurationToFolder(configFolder, licFolder);

        //when
        configurationService.writeAllConfigurationToFolder(configFolder, licFolder);

        // then
        Assertions.assertThat(configFolder.toPath().resolve("platform_type").resolve("conf1.properties").toFile())
                .as("should lowercase configuration type").exists();
        Assertions
                .assertThat(configFolder.toPath().resolve("tenants").resolve("5").resolve("tenant_type")
                        .resolve("conf2.properties").toFile())
                .as("should create sub folder with tenantId").exists();

    }

    @Test
    public void should_prevent_from_deleting_non_tenant_conf() {
        //expects
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("tenantId value 0 is not allowed");

        //when
        configurationService.deleteTenantConfiguration(0);
    }

    @Test
    public void storeAllConfiguration_should_report_a_timeout_when_the_statement_is_cancelled() throws Exception {
        //given a PostgreSQL statement cancelled by its timeout, as translated by Spring
        final File configFolder = temporaryFolder.newFolder("config");
        DataAccessResourceFailureException cancelled = new DataAccessResourceFailureException("cancelled",
                new SQLException("canceling statement due to user request", "57014"));
        ConfigurationServiceImpl service = serviceFailingWith(cancelled);

        //when-then
        Assertions.assertThatThrownBy(() -> service.storeAllConfiguration(configFolder.toPath()))
                .isInstanceOf(PlatformException.class)
                .hasMessageContaining("Unable to store the platform configuration")
                .hasMessageContaining("did not complete the write in time")
                .hasMessageContaining(ConfigurationService.TRANSACTION_TIMEOUT_PROPERTY)
                .hasCause(cancelled);
    }

    @Test
    public void storeLicenses_should_report_a_timeout_when_the_lock_cannot_be_acquired() throws Exception {
        //given
        final File licensesFolder = temporaryFolder.newFolder("licenses");
        CannotAcquireLockException lockException = new CannotAcquireLockException("lock wait timeout");
        ConfigurationServiceImpl service = serviceFailingWith(lockException);

        //when-then
        Assertions.assertThatThrownBy(() -> service.storeLicenses(licensesFolder))
                .isInstanceOf(PlatformException.class)
                .hasMessageContaining("Unable to store the platform licenses")
                .hasMessageContaining("did not complete the write in time")
                .hasCause(lockException);
    }

    @Test
    public void storeAllConfiguration_should_report_a_timeout_even_when_the_rollback_fails() throws Exception {
        //given the write times out, then the rollback fails on the closed connection (seen with H2)
        final File configFolder = temporaryFolder.newFolder("config-rollback-failure");
        TransactionSystemException rollbackFailure = new TransactionSystemException("JDBC rollback failed",
                new SQLException("Connection is closed"));
        rollbackFailure.initApplicationException(new CannotAcquireLockException("lock wait timeout"));
        ConfigurationServiceImpl service = serviceFailingWith(rollbackFailure);

        //when-then
        Assertions.assertThatThrownBy(() -> service.storeAllConfiguration(configFolder.toPath()))
                .isInstanceOf(PlatformException.class)
                .hasMessageContaining("did not complete the write in time")
                .hasMessageContaining("lock wait timeout")
                .hasCause(rollbackFailure);
    }

    @Test
    public void storeAllConfiguration_should_report_the_cause_when_no_transaction_can_be_created() throws Exception {
        //given the database is down: the transaction manager wraps the failure in a TransactionException
        final File configFolder = temporaryFolder.newFolder("config-db-down");
        CannotCreateTransactionException dbDown = new CannotCreateTransactionException("Could not open connection",
                new SQLException("Connection refused", "08001"));
        ConfigurationServiceImpl service = serviceFailingWith(dbDown);

        //when-then
        Assertions.assertThatThrownBy(() -> service.storeAllConfiguration(configFolder.toPath()))
                .isInstanceOf(PlatformException.class)
                .hasMessageContaining("Unable to store the platform configuration")
                .hasMessageContaining("Connection refused")
                .hasMessageNotContaining("in time")
                .hasCause(dbDown);
    }

    @Test
    public void isLockOrTimeoutFailure_should_recognize_timeouts_of_every_database() {
        Assertions.assertThat(ConfigurationServiceImpl.isLockOrTimeoutFailure(
                new UncategorizedSQLException("update", "DELETE", new SQLTimeoutException("timeout")))).isTrue();
        Assertions.assertThat(ConfigurationServiceImpl.isLockOrTimeoutFailure(
                new QueryTimeoutException("timeout"))).isTrue();
        Assertions.assertThat(ConfigurationServiceImpl.isLockOrTimeoutFailure(
                new TransactionTimedOutException("deadline reached"))).isTrue();
        Assertions.assertThat(ConfigurationServiceImpl.isLockOrTimeoutFailure(
                new DataAccessResourceFailureException("cancelled", new SQLException("cancelled", "57014"))))
                .isTrue();
    }

    @Test
    public void isLockOrTimeoutFailure_should_not_recognize_other_failures() {
        Assertions.assertThat(ConfigurationServiceImpl.isLockOrTimeoutFailure(
                new DataAccessResourceFailureException("down", new SQLException("Connection refused", "08001"))))
                .isFalse();
        Assertions.assertThat(ConfigurationServiceImpl.isLockOrTimeoutFailure(
                new BadSqlGrammarException("update", "DELETE", new SQLException("syntax error", "42601"))))
                .isFalse();
    }

    private static ConfigurationServiceImpl serviceFailingWith(RuntimeException failure) {
        PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);
        Mockito.doThrow(failure).when(transactionManager).getTransaction(ArgumentMatchers.any());
        return new ConfigurationServiceImpl(Mockito.mock(JdbcTemplate.class),
                new TransactionTemplate(transactionManager), "h2");
    }
}
