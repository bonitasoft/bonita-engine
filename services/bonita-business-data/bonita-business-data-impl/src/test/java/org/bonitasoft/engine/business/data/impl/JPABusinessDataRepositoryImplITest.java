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

import static com.company.pojo.EmployeeBuilder.anEmployee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionService;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import bitronix.tm.TransactionManagerServices;

import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.company.pojo.Employee;
import com.company.pojo.Person;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
public class JPABusinessDataRepositoryImplITest {

    private JPABusinessDataRepositoryImpl businessDataRepository;

    private TransactionService transactionService;

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
        initMocks(this);
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(datasource);
        }

        transactionService = mock(TransactionService.class);
        final SchemaManager schemaManager = new SchemaManager(modelConfiguration, mock(TechnicalLoggerService.class));
        final BusinessDataModelRepositoryImpl businessDataModelRepositoryImpl = spy(new BusinessDataModelRepositoryImpl(mock(DependencyService.class),
                schemaManager, 1L));
        businessDataRepository = spy(new JPABusinessDataRepositoryImpl(transactionService, businessDataModelRepositoryImpl, configuration));
        doReturn(true).when(businessDataModelRepositoryImpl).isDBMDeployed();
        ut = TransactionManagerServices.getTransactionManager();
        ut.begin();

        final Set<String> classNames = new HashSet<String>();
        classNames.add(Employee.class.getName());
        classNames.add(Person.class.getName());

        businessDataModelRepositoryImpl.update(classNames);
        businessDataRepository.start();
    }

    @After
    public void tearDown() throws Exception {
        ut.rollback();
        businessDataRepository.stop();

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(modelDatasource);
        for (final String tableName : Arrays.asList("Person_nickNames", "Employee", "PERSON")) {
            try {
                jdbcTemplate.update("drop table " + tableName);
            } catch (final Exception e) {
                System.out.println(e.getMessage());
                // ignore drop of non-existing table
            }
        }
    }

    private Employee addEmployeeToRepository(final Employee employee) {
        businessDataRepository.persist(employee);
        return employee;
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwAnExceptionIfTheIdentifierIsNull() throws Exception {
        businessDataRepository.findById(Employee.class, null);
    }

    @Test
    public void findAnEmployeeByPrimaryKey() throws Exception {
        Employee expectedEmployee = anEmployee().build();
        expectedEmployee = addEmployeeToRepository(expectedEmployee);

        final Employee employee = businessDataRepository.findById(Employee.class, expectedEmployee.getPersistenceId());

        assertThat(employee).isEqualTo(expectedEmployee);
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void throwExceptionWhenEmployeeNotFound() throws Exception {
        businessDataRepository.findById(Employee.class, -145l);
    }

    @Test
    public void persistNewEmployeeShouldAddEmployeeInRepository() throws Exception {
        final Employee employee = anEmployee().build();
        businessDataRepository.persist(employee);

        final Employee myEmployee = businessDataRepository.findById(Employee.class, employee.getPersistenceId());
        assertThat(myEmployee).isEqualTo(employee);
    }

    @Test
    public void persistANullEmployeeShouldDoNothing() throws Exception {
        businessDataRepository.persist(null);

        final Long count = businessDataRepository.find(Long.class, "SELECT COUNT(*) FROM Employee e", null);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void findListShouldAcceptParameterizedQuery() throws Exception {
        final String firstName = "anyName";
        Employee expectedEmployee = anEmployee().withFirstName(firstName).build();
        expectedEmployee = addEmployeeToRepository(expectedEmployee);

        final Map<String, Serializable> parameters = Collections.singletonMap("firstName", (Serializable) firstName);
        final Employee matti = businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.firstName = :firstName", parameters);

        assertThat(matti).isEqualTo(expectedEmployee);
    }

    @Test(expected = NonUniqueResultException.class)
    public void findShouldThrowExceptionWhenSeveralResultsMatch() throws Exception {
        final String lastName = "Kangaroo";
        addEmployeeToRepository(anEmployee().withLastName(lastName).build());
        addEmployeeToRepository(anEmployee().withLastName(lastName).build());

        final Map<String, Serializable> parameters = Collections.singletonMap("lastName", (Serializable) lastName);
        businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters);
    }

    @Test
    public void should_get_employees_by_id() throws Exception {
        final String lastName = "Kangaroo";
        Employee emp1 = addEmployeeToRepository(anEmployee().withLastName(lastName).build());
        Employee emp2 = addEmployeeToRepository(anEmployee().withLastName(lastName).build());
        Employee emp3 = addEmployeeToRepository(anEmployee().withLastName(lastName).build());
        
        List<Employee> emps = businessDataRepository.findByIds(Employee.class, Arrays.asList(emp1.getPersistenceId(), emp2.getPersistenceId()));
        
        assertThat(emps).contains(emp1, emp2);
        assertThat(emps).doesNotContain(emp3);
    }
    
    @Test
    public void should_return_an_empty_list_when_getting_entities_with_empty_ids_list() throws Exception {
       ArrayList<Long> emptyIdsList = new ArrayList<Long>();
        
        List<Employee> emps = businessDataRepository.findByIds(Employee.class, emptyIdsList);
        
        assertThat(emps).isEmpty();
    }
    
    @Test
    public void returnNullnWhenFindingAnUnknownEmployee() throws Exception {
        final Map<String, Serializable> parameters = Collections.singletonMap("lastName", (Serializable) "Unknown_lastName");
        assertThat(businessDataRepository.find(Employee.class, "FROM Employee e WHERE e.lastName = :lastName", parameters)).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionWhenUsingBDRWihtoutStartingIt() throws Exception {
        businessDataRepository.stop();

        businessDataRepository.findById(Employee.class, 124L);

        businessDataRepository.start();
    }

    @Test
    public void entityClassNames_is_an_empty_set_if_bdr_is_not_started() {
        businessDataRepository.stop();

        final Set<String> classNames = businessDataRepository.getEntityClassNames();

        assertThat(classNames).isEmpty();
    }

    @Test
    public void updateTwoFieldsInSameTransactionShouldModifySameObject() throws Exception {
        final Employee originalEmployee = addEmployeeToRepository(anEmployee().build());
        originalEmployee.setLastName("NewLastName");
        originalEmployee.setFirstName("NewFirstName");

        final Employee updatedEmployee = businessDataRepository.findById(Employee.class, originalEmployee.getPersistenceId());
        assertThat(updatedEmployee).isEqualTo(originalEmployee);
    }

    @Test
    public void getEntityClassNames_should_return_the_classes_managed_by_the_bdr() {
        final Set<String> classNames = businessDataRepository.getEntityClassNames();

        assertThat(classNames).containsExactly(Employee.class.getName(), Person.class.getName());
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void aRemovedEntityShouldNotBeRetrievableAnyLonger() throws SBusinessDataNotFoundException {
        Employee employee = null;
        try {
            employee = addEmployeeToRepository(anEmployee().build());

            businessDataRepository.remove(employee);
        } catch (final Exception e) {
            fail("Should not fail here");
        }
        if (employee != null) {
            businessDataRepository.findById(Employee.class, employee.getPersistenceId());
        }
    }

    @Test
    public void remove_should_not_throw_an_exception_with_a_null_entity() {
        businessDataRepository.remove(null);
    }

    @Test
    public void remove_should_not_throw_an_exception_with_an_unknown_entity_without_an_id() {
        businessDataRepository.remove(anEmployee().build());
    }

    @Test
    public void remove_should_not_throw_an_exception_with_an_unknown_entity() {
        final Employee newEmployee = addEmployeeToRepository(anEmployee().build());
        businessDataRepository.remove(newEmployee);
        businessDataRepository.remove(newEmployee);
    }

    @Test
    public void findList_should_return_employee_list() {
        final Employee e1 = addEmployeeToRepository(anEmployee().withFirstName("Hannu").withLastName("balou").build());
        final Employee e2 = addEmployeeToRepository(anEmployee().withFirstName("Aliz").withLastName("akkinen").build());
        final Employee e3 = addEmployeeToRepository(anEmployee().withFirstName("Jean-Luc").withLastName("akkinen").build());

        final List<Employee> employees = businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e ORDER BY e.lastName ASC, e.firstName ASC",
                null, 0, 10);

        assertThat(employees).containsExactly(e2, e3, e1);
    }

    @Test
    public void findListShouldReturnEmptyListIfNoResults() {
        final Map<String, Serializable> parameters = Collections.singletonMap("firstName", (Serializable) "Jaakko");
        final List<Employee> employees = businessDataRepository.findList(Employee.class,
                "SELECT e FROM Employee e WHERE e.firstName=:firstName ORDER BY e.lastName, e.firstName", parameters, 0, 10);
        assertThat(employees).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findListShouldThrowAnExceptionIfAtLeastOneQueryParameterIsNotSet() {
        businessDataRepository.findList(Employee.class, "SELECT e FROM Employee e WHERE e.firstName=:firstName ORDER BY e.lastName, e.firstName", null, 0, 10);
    }

    @Test
    public void findBasedOnAMultipleAttributeShouldReturnTheEntity() throws Exception {
        final Person person = new Person();
        person.addTo("John");
        person.addTo("James");
        person.addTo("Jack");
        businessDataRepository.persist(person);

        final Person actual = businessDataRepository.find(Person.class, "SELECT p FROM Person p WHERE 'James' IN ELEMENTS(p.nickNames)",
                null);
        assertThat(actual).isEqualTo(person);
        actual.getNickNames().remove("James");

        final Person actual2 = businessDataRepository.find(Person.class, "SELECT p FROM Person p WHERE 'James' IN ELEMENTS(p.nickNames)", null);
        assertThat(actual2).isNull();
    }

    @Test
    public void getEntityManagerAddATransactionSynchroInOrderToCleanTheThreadLocalWhenTheTxIsOver() throws Exception {
        businessDataRepository.getEntityManager();

        verify(transactionService).registerBonitaSynchronization(any(RemoveEntityManagerSynchronization.class));
    }

}
