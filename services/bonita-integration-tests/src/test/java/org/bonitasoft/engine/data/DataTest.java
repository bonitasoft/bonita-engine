package org.bonitasoft.engine.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.data.model.Address;
import org.bonitasoft.engine.data.model.Employee;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.junit.Test;

public class DataTest extends CommonServiceTest {

    private static DataService dataService;

    private static SDataSourceBuilder dataSourceModelBuilder;

    static {
        dataService = getServicesBuilder().buildDataService();
        dataSourceModelBuilder = getServicesBuilder().buildDataSourceModelBuilder();
    }

    // tester a tout prix
    // les configurations
    // la multi tenancy
    // impl hibernate + mybatis
    // une version DB pour voir comment le configure impact l'impl d'une
    // datasource client

    @Test
    public void testInsertEmployee() throws Exception {
        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        getTransactionService().begin();

        dataService.createDataSource(dataSource);

        final EmployeeDataSource employeeDataSource = dataService.getDataSourceImplementation(EmployeeDataSource.class, dataSource.getId());

        final Employee employee = new Employee(1, "firstName", "lastName", 30);
        final Address employeeAddress1 = new Address(1, "street", "city", 75000);
        employee.addAddress(employeeAddress1);

        employeeDataSource.insertEmployee(employee);
        getTransactionService().complete();

        getTransactionService().begin();
        assertNotNull(employeeDataSource.getEmployee(employee.getId()));

        dataService.removeDataSource(dataSource.getId());
        getTransactionService().complete();
    }

    @Test
    public void testGetDataSource() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);

        assertNotNull(dataService.getDataSource(dataSource.getId()));
        assertNotNull(dataService.getDataSource("employee", "1.0"));

        assertEquals("employee", dataService.getDataSource(dataSource.getId()).getName());
        assertEquals("1.0", dataService.getDataSource(dataSource.getId()).getVersion());
        assertEquals(SDataSourceState.ACTIVE, dataService.getDataSource(dataSource.getId()).getState());
        assertEquals(EmployeeDataSourceImpl.class.getName(), dataService.getDataSource(dataSource.getId()).getImplementationClassName());

        dataService.removeDataSource(dataSource);
        getTransactionService().complete();
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public void testRemoveDataSource() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource1 = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource1);
        dataService.removeDataSource(dataSource1.getId());

        dataService.getDataSource(dataSource1.getId());
        getTransactionService().complete();

        getTransactionService().begin();

        final SDataSource dataSource2 = dataSourceModelBuilder.createNewInstance("employee", "1.1", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource2);
        dataService.removeDataSource(dataSource2);

        dataService.getDataSource(dataSource2.getId());
        getTransactionService().complete();
    }

}
