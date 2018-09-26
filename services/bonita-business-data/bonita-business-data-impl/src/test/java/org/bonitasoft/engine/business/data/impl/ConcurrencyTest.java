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
package org.bonitasoft.engine.business.data.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import bitronix.tm.TransactionManagerServices;
import com.company.pojo.Employee;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
public class ConcurrencyTest {

    public static final long TENANT_ID = 654643L;
    private JPABusinessDataRepositoryImpl businessDataRepository;

    @Autowired
    @Qualifier("businessDataDataSource")
    private DataSource datasource;

    @Autowired
    @Qualifier("notManagedBizDataSource")
    private DataSource modelDatasource;

    @Resource(name = "jpa-configuration")
    private Map<String, Object> configuration;

    @Resource(name = "jpa-model-configuration")
    private Map<String, Object> modelConfiguration;

    private JdbcTemplate jdbcTemplate;

    private UserTransaction ut;

    private ClassLoaderService classLoaderService = mock(ClassLoaderService.class);

    @BeforeClass
    public static void initializeBitronix() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
        TransactionManagerServices.getConfiguration().setJournal(null);
    }

    @AfterClass
    public static void shutdownTransactionManager() {
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Before
    public void setUp() throws Exception {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(datasource);
        }

        final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);
        doReturn(mock(TechnicalLogger.class)).when(loggerService).asLogger(any());
        final SchemaManagerUpdate schemaManager = new SchemaManagerUpdate(modelConfiguration, loggerService);
        final BusinessDataModelRepositoryImpl businessDataModelRepositoryImpl = spy(new BusinessDataModelRepositoryImpl(mock(DependencyService.class),
                schemaManager, mock(TenantResourcesService.class), TENANT_ID));
        final UserTransactionService transactionService = mock(UserTransactionService.class);
        businessDataRepository = spy(
                new JPABusinessDataRepositoryImpl(transactionService, businessDataModelRepositoryImpl, loggerService, configuration, classLoaderService, 1L));
        doReturn(true).when(businessDataModelRepositoryImpl).isBDMDeployed();

        ut = TransactionManagerServices.getTransactionManager();
        ut.begin();

        final Set<String> classNames = new HashSet<>();
        classNames.add(Employee.class.getName());

        businessDataModelRepositoryImpl.update(classNames);
        businessDataRepository.start();
        ut.commit();
    }

    @After
    public void tearDown() {
        businessDataRepository.stop();

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(modelDatasource);
        try {
            jdbcTemplate.update("drop table Employee");
        } catch (final Exception e) {
            // ignore drop of non-existing table
        }
    }

    @Test
    public void addConcurrentlyEmployeesShouldCreateAllTheEmployees() throws Exception {
        final ExecutorService threadPoolExecutor = new ThreadPoolExecutor(5, 10, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        final int expected = 10;
        for (int i = 0; i < expected; i++) {
            final Thread thread = new AddNewEmployeeThread(businessDataRepository, i);
            threadPoolExecutor.submit(thread);
        }
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(3 * expected, TimeUnit.SECONDS);

        ut = TransactionManagerServices.getTransactionManager();
        ut.begin();
        final List<Employee> employees = businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e", null, 0, 100);
        ut.commit();

        assertThat(employees).hasSize(expected);
    }

}
