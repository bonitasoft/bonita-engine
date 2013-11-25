package com.bonitasoft.engine.business.data.impl;

import static org.fest.assertions.Assertions.assertThat;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.NonUniqueResultException;

public class BusinessDataRepositoryImplTest {

    private IDatabaseTester databaseTester;

    private static PoolingDataSource ds1;

    @BeforeClass
    public static void createDatasource() throws NamingException, SQLException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
        TransactionManagerServices.getConfiguration().setJournal(null);

        ds1 = new PoolingDataSource();
        ds1.setUniqueName("java:/comp/env/jdbc/PGDS1");
        ds1.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds1.setMaxPoolSize(3);
        ds1.setAllowLocalTransactions(true);
        ds1.getDriverProperties().put("URL", "jdbc:h2:mem:database;LOCK_MODE=0;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;");
        ds1.getDriverProperties().put("user", "sa");
        ds1.getDriverProperties().put("password", "");
        ds1.init();
    }

    @AfterClass
    public static void closeDataSource() {
        ds1.close();
    }

    // @Before
    public void setUp() throws Exception {
        databaseTester = new DataSourceDatabaseTester(ds1);
        final InputStream stream = BusinessDataRepositoryImplTest.class.getResourceAsStream("/dataset.xml");
        final FlatXmlDataSet dataSet = new FlatXmlDataSetBuilder().build(stream);
        stream.close();
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @After
    public void tearDown() throws Exception {
        if (databaseTester != null) {
            databaseTester.onTearDown();
        }
    }

    @Test
    public void findAnEmployeeByPrimaryKey() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            setUp();
            final Employee employee = businessDataRepositoryImpl.find(Employee.class, 45l);
            assertThat(employee).isNotNull();
            assertThat(employee.getId()).isEqualTo(45l);
            assertThat(employee.getFirstName()).isEqualTo("Hannu");
            assertThat(employee.getLastName()).isEqualTo("Hakkinen");
        } finally {
            ut.commit();
        }
    }

    @Test(expected = BusinessDataNotFoundException.class)
    public void throwExceptionWhenEmployeeNotFound() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            businessDataRepositoryImpl.find(Employee.class, -145l);
        } finally {
            ut.commit();
        }
    }

    @Test
    public void persistNewEmployee() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        final Employee employee = new Employee("Marja", "Halonen");
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            businessDataRepositoryImpl.persist(employee);
        } finally {
            ut.commit();
        }
        assertThat(employee.getId()).isNotNull();

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepositoryImpl.find(Employee.class, employee.getId());
        } finally {
            ut.commit();
        }
    }

    @Test
    public void persistANullEmployee() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            businessDataRepositoryImpl.persist(null);
            final Long count = businessDataRepositoryImpl.find(Long.class, "SELECT COUNT(*) FROM Employee e", null);
            assertThat(count).isEqualTo(0);
        } finally {
            ut.commit();
        }
    }

    @Test
    public void findAnEmployeeUsingParameterizedQuery() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            setUp();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepositoryImpl.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            assertThat(matti.getFirstName()).isEqualTo("Matti");
        } finally {
            ut.commit();
        }
    }

    @Test(expected = NonUniqueResultException.class)
    public void throwExceptionWhenFindingAnEmployeeButGettingSeveral() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            setUp();
            final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Hakkinen");
            businessDataRepositoryImpl.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
        } finally {
            ut.commit();
        }
    }

    @Test(expected = BusinessDataNotFoundException.class)
    public void throwExceptionWhenFindingAnUnknownEmployee() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final BusinessDataRepositoryImpl businessDataRepositoryImpl = new BusinessDataRepositoryImpl();
        try {
            ut.begin();
            businessDataRepositoryImpl.start();
            setUp();
            final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Makkinen");
            businessDataRepositoryImpl.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
        } finally {
            ut.commit();
        }
    }

}
