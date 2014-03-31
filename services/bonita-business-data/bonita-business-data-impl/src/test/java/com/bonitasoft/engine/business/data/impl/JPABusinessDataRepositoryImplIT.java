package com.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import bitronix.tm.TransactionManagerServices;

import com.bonitasoft.engine.business.data.NonUniqueResultException;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.pojo.Employee;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
public class JPABusinessDataRepositoryImplIT {

    private JPABusinessDataRepositoryImpl businessDataRepository;

    @Mock
    private DependencyService dependencyService;

    @Mock
    private TechnicalLoggerService loggerService;

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

    @BeforeClass
    public static void initializeBitronix() throws NamingException, SQLException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "bitronix.tm.jndi.BitronixInitialContextFactory");
        TransactionManagerServices.getConfiguration().setJournal(null);

        // addEmployeeToRepository(buildEmployee(45L, "Hannu", "Hakkinen"));
        // addEmployeeToRepository(buildEmployee(23L, "Petteri", "Salo"));
        // addEmployeeToRepository(buildEmployee(75L, "Matti", "Hakkinen"));

    }

    @AfterClass
    public static void shutdownTransactionManager() {
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(datasource);
        }

        businessDataRepository = spy(new JPABusinessDataRepositoryImpl(dependencyService, loggerService, configuration, modelConfiguration));
        doReturn(null).when(businessDataRepository).createSDependency(anyLong(), any(byte[].class));
        doReturn(null).when(businessDataRepository).createDependencyMapping(anyLong(), any(SDependency.class));
        doReturn(true).when(businessDataRepository).isDBMDeployed();
        // doNothing().when(businessDataRepository).updateSchema();

    }

    @After
    public void tearDown() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(modelDatasource);
        try {
            jdbcTemplate.update("drop table Employee");
        } catch (Exception e) {
            // ignore drop of non-existing table
        }
    }

    @Test
    public void findAnEmployeeByPrimaryKey() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                try {
                    Employee expectedEmployee = buildEmployee(45L, "Hannu", "Hakkinen");
                    addEmployeeToRepository(expectedEmployee);

                    final Employee employee = businessDataRepository.findById(Employee.class, 45l);

                    assertThat(employee).isEqualTo(expectedEmployee);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwExceptionWhenEmployeeNotFound() throws Exception {
        executeInTransaction(new RunnableInTransaction(false) {

            @Override
            public void run() {
                try {
                    businessDataRepository.findById(Employee.class, -145l);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test
    public void persistNewEmployee() throws Exception {
        executeInTransaction(new RunnableInTransaction(false) {

            @Override
            public void run() {
                try {
                    final Employee employee = businessDataRepository.merge(new Employee("Marja", "Halonen"));
                    assertThat(employee.getPersistenceId()).isNotNull();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test
    public void persistANullEmployee() throws Exception {
        executeInTransaction(new RunnableInTransaction(false) {

            @Override
            public void run() {
                try {
                    businessDataRepository.merge(null);
                    final Long count = businessDataRepository.find(Long.class, "SELECT COUNT(*) FROM Employee e", null);
                    assertThat(count).isEqualTo(0);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test
    public void findAnEmployeeUsingParameterizedQuery() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                Employee expectedEmployee = buildEmployee(46L, "Matti", "Hakkinen");
                addEmployeeToRepository(expectedEmployee);

                final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
                try {
                    final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
                    assertThat(matti).isEqualTo(expectedEmployee);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    @Test(expected = NonUniqueResultException.class)
    public void throwExceptionWhenFindingAnEmployeeButGettingSeveral() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                addEmployeeToRepository(buildEmployee(45L, "Hannu", "Hakkinen"));
                addEmployeeToRepository(buildEmployee(41L, "Alfred", "Hakkinen"));

                final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Hakkinen");
                try {
                    businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwExceptionWhenFindingAnUnknownEmployee() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Unknown_lastName");
                try {
                    businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionWhenUsingBDRWihtoutStartingIt() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("lastName", (Object) "Makkinen");
            businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
        } finally {
            ut.commit();
        }
    }

    @Test
    public void entityClassNames_is_an_empty_set_if_bdr_is_not_started() throws Exception {

        final Set<String> classNames = businessDataRepository.getEntityClassNames();

        assertThat(classNames).isEmpty();
    }

    @Test
    public void entityClassNames_contains_all_entities_class_names() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();

            final Set<String> classNames = businessDataRepository.getEntityClassNames();

            assertThat(classNames).containsOnly("com.bonitasoft.pojo.Employee");
        } finally {
            ut.commit();
        }
    }

    @Test
    public void updateTwoFieldsInSameTransactionShouldModifySameObject() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        final Employee matti1;
        try {
            ut.begin();
            businessDataRepository.start();

            Employee expectedEmployee = buildEmployee(47L, "Matti", "Hakkinen");
            addEmployeeToRepository(expectedEmployee);

            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            matti1 = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            matti1.setLastName("NewLastName");
            businessDataRepository.merge(matti1);
            matti1.setFirstName("NewFirstName");
            businessDataRepository.merge(matti1);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "NewFirstName");
            final Employee matti2 = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            assertThat(matti2).isEqualTo(matti1);
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    @Test
    public void updateAnEmployeeUsingParameterizedQuery() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            Employee expectedEmployee = buildEmployee(45L, "Matti", "not-important");
            addEmployeeToRepository(expectedEmployee);

            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            matti.setLastName("Halonen");
            businessDataRepository.merge(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            assertThat(matti.getLastName()).isEqualTo("Halonen");
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    /**
     * Sets up the database (if specified), the businessDataRepository, and runs a piece of business logics inside a transaction.
     * 
     * @param runnable
     *            the logics to run.
     */
    private void executeInTransaction(final RunnableInTransaction runnable) throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            if (runnable.isSetupDatabase()) {
                // setUpDatabase();
            }

            runnable.run();
        } catch (final RuntimeException e) {
            throw (Exception) e.getCause();
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }

    }

    private static abstract class RunnableInTransaction implements Runnable {

        private final boolean setupDatabase;

        public RunnableInTransaction(final boolean setupDatabase) {
            this.setupDatabase = setupDatabase;
        }

        public boolean isSetupDatabase() {
            return setupDatabase;
        }
    }

    @Test
    public void getEntityClassNames_returns_the_classes_managed_by_the_bdr() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                final Set<String> expected = Collections.singleton(Employee.class.getName());

                final Set<String> entityClassNames = businessDataRepository.getEntityClassNames();
                assertThat(entityClassNames).isEqualTo(expected);
            }
        });
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void removeAnEntity() throws Exception {
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            businessDataRepository.remove(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Matti");
            businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            fail("The employee was removed previously");
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    @Test
    public void remove_should_not_throw_an_exception_with_a_null_entity() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                businessDataRepository.remove(null);
            }
        });
    }

    @Test
    public void remove_should_not_throw_an_exception_with_an_unknown_entity_without_an_id() throws Exception {
        executeInTransaction(new RunnableInTransaction(true) {

            @Override
            public void run() {
                businessDataRepository.remove(new Employee("Tarja", "Makkinen"));
            }
        });
    }

    public void addEmployeeToRepository(final Employee employee) {
        String sql = "INSERT INTO Employee (PERSISTENCEID, FIRSTNAME, LASTNAME) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, new Object[] { employee.getPersistenceId(), employee.getFirstName(), employee.getLastName() });
    }

    private Employee buildEmployee(final Long persistenceId, final String firstName, final String lastName) {
        return new Employee(persistenceId, firstName, lastName);
    }

    @Test
    public void remove_should_not_throw_an_exception_with_an_unknown_entity() throws Exception {
        Employee matti;
        String firstName = "Matti";
        UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            addEmployeeToRepository(buildEmployee(17L, firstName, "Hakkinen"));
            // setUpDatabase();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) firstName);
            matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);
            businessDataRepository.remove(matti);
        } finally {
            ut.commit();
        }

        ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.remove(matti);
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ut.commit();
            businessDataRepository.stop();
        }
    }

    @Test
    public void findList_should_return_employee_list() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            Employee e1 = buildEmployee(45L, "Hannu", "Balou");
            Employee e2 = buildEmployee(41L, "Aliz", "akkinen");
            Employee e3 = buildEmployee(51L, "Jean-Luc", "akkinen");
            addEmployeeToRepository(e1);
            addEmployeeToRepository(e2);
            addEmployeeToRepository(e3);
            final List<Employee> employees = businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e ORDER BY e.lastName, e.firstName", null);
            assertThat(employees).containsExactly(e2, e3, e1);

        } finally {
            ut.commit();
        }
    }

    @Test
    public void findList_should_return_an_empty() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            final Map<String, Object> parameters = Collections.singletonMap("firstName", (Object) "Jaakko");
            final List<Employee> employees = businessDataRepository.findList(Employee.class,
                    "SELECT e FROM Employee e WHERE e.firstName=:firstName ORDER BY e.lastName, e.firstName", parameters);
            assertThat(employees).isEmpty();

        } finally {
            ut.commit();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void findList_should_throw_an_exception_if_parameter_is_not_set() throws Exception {
        final UserTransaction ut = TransactionManagerServices.getTransactionManager();
        try {
            ut.begin();
            businessDataRepository.start();
            businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e WHERE e.firstName=:firstName ORDER BY e.lastName, e.firstName", null);
        } finally {
            ut.commit();
        }
    }

}
