package org.bonitasoft.engine.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.data.model.Address;
import org.bonitasoft.engine.data.model.Employee;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceParameter;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceParameterBuilder;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;

public class DataTest extends CommonServiceTest {

    private static DataService dataService;

    private static SDataSourceBuilder dataSourceModelBuilder;

    private static SDataSourceParameterBuilder dataSourceParameterModelBuilder;

    static {
        dataService = getServicesBuilder().buildDataService();
        dataSourceModelBuilder = getServicesBuilder().buildDataSourceModelBuilder();
        dataSourceParameterModelBuilder = getServicesBuilder().buildDataSourceParameterModelBuilder();
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
    public void testCreateDataSourceParameter() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);
        final SDataSourceParameter dataSourceParameter = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name",
                "domain_value").done();
        dataService.createDataSourceParameter(dataSourceParameter);
        getTransactionService().complete();

        getTransactionService().begin();

        assertNotNull(dataService.getDataSourceParameter(dataSourceParameter.getId()));

        dataService.removeDataSourceParameter(dataSourceParameter.getId());
        dataService.removeDataSource(dataSource);
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

    @Test
    public void testCreateDataSourceParameters() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);
        final SDataSourceParameter dataSourceParameter1 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name1",
                "domain_value1").done();
        final SDataSourceParameter dataSourceParameter2 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name2",
                "domain_value2").done();
        final SDataSourceParameter dataSourceParameter3 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name3",
                "domain_value3").done();
        final List<SDataSourceParameter> dataSourceParameterList = new ArrayList<SDataSourceParameter>();
        dataSourceParameterList.add(dataSourceParameter1);
        dataSourceParameterList.add(dataSourceParameter2);
        dataSourceParameterList.add(dataSourceParameter3);
        dataService.createDataSourceParameters(dataSourceParameterList);
        getTransactionService().complete();

        getTransactionService().begin();
        assertEquals(3, dataService.getDataSourceParameters(dataSource.getId(), QueryOptions.allResultsQueryOptions()).size());

        dataService.removeDataSourceParameters(dataSource.getId());
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

    @Test
    public void testRemoveDataSourceParameter() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);
        final SDataSourceParameter dataSourceParameter1 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name1",
                "domain_value1").done();
        final SDataSourceParameter dataSourceParameter2 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name3",
                "domain_value2").done();
        final List<SDataSourceParameter> dataSourceParameterList = new ArrayList<SDataSourceParameter>();
        dataSourceParameterList.add(dataSourceParameter1);
        dataSourceParameterList.add(dataSourceParameter2);
        dataService.createDataSourceParameters(dataSourceParameterList);
        getTransactionService().complete();

        getTransactionService().begin();
        dataService.removeDataSourceParameter(dataSourceParameter1);
        assertEquals(1, dataService.getDataSourceParameters(dataSource.getId(), QueryOptions.allResultsQueryOptions()).size());

        dataService.removeDataSourceParameter(dataSourceParameter2.getId());
        assertEquals(0, dataService.getDataSourceParameters(dataSource.getId(), QueryOptions.allResultsQueryOptions()).size());

        dataService.removeDataSource(dataSource);
        getTransactionService().complete();
    }

    @Test
    public void testRemoveDataSourceParameters() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);
        final SDataSourceParameter dataSourceParameter1 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name1",
                "domain_value1").done();
        final SDataSourceParameter dataSourceParameter2 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name3",
                "domain_value2").done();
        final SDataSourceParameter dataSourceParameter3 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name2",
                "domain_value3").done();
        final SDataSourceParameter dataSourceParameter4 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name4",
                "domain_value4").done();
        final SDataSourceParameter dataSourceParameter5 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name5",
                "domain_value5").done();
        final List<SDataSourceParameter> dataSourceParameterList = new ArrayList<SDataSourceParameter>();
        dataSourceParameterList.add(dataSourceParameter1);
        dataSourceParameterList.add(dataSourceParameter2);
        dataSourceParameterList.add(dataSourceParameter3);
        dataSourceParameterList.add(dataSourceParameter4);
        dataSourceParameterList.add(dataSourceParameter5);
        dataService.createDataSourceParameters(dataSourceParameterList);
        getTransactionService().complete();

        getTransactionService().begin();

        final Collection<Long> dataSourceParameterIds = new ArrayList<Long>();
        dataSourceParameterIds.add(dataService.getDataSourceParameter("domain_name1", dataSource.getId()).getId());
        dataSourceParameterIds.add(dataService.getDataSourceParameter("domain_name2", dataSource.getId()).getId());
        dataService.removeDataSourceParameters(dataSourceParameterIds);
        assertEquals(3, dataService.getDataSourceParameters(dataSource.getId(), QueryOptions.allResultsQueryOptions()).size());

        dataService.removeDataSourceParameters(dataSource.getId());
        assertEquals(0, dataService.getDataSourceParameters(dataSource.getId(), QueryOptions.allResultsQueryOptions()).size());

        dataService.removeDataSource(dataSource);
        getTransactionService().complete();
    }

    @Test
    public void testGetDataSourceParameter() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);
        final SDataSourceParameter dataSourceParameter = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name",
                "domain_value").done();
        dataService.createDataSourceParameter(dataSourceParameter);
        getTransactionService().complete();

        getTransactionService().begin();

        assertNotNull(dataService.getDataSourceParameter(dataSourceParameter.getId()));
        assertNotNull(dataService.getDataSourceParameter("domain_name", dataSource.getId()));
        assertEquals("domain_name", dataService.getDataSourceParameter(dataSourceParameter.getId()).getName());
        assertEquals("domain_value", dataService.getDataSourceParameter(dataSourceParameter.getId()).getValue_());
        assertEquals(dataSource.getId(), dataService.getDataSourceParameter(dataSourceParameter.getId()).getDataSourceId());

        dataService.removeDataSourceParameter(dataSourceParameter.getId());
        dataService.removeDataSource(dataSource);
        getTransactionService().complete();
    }

    @Test
    public void testGetDataSourceParameters() throws Exception {
        getTransactionService().begin();

        final SDataSource dataSource = dataSourceModelBuilder.createNewInstance("employee", "1.0", SDataSourceState.ACTIVE,
                EmployeeDataSourceImpl.class.getName()).done();
        dataService.createDataSource(dataSource);
        final SDataSourceParameter dataSourceParameter1 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name1",
                "domain_value1").done();
        final SDataSourceParameter dataSourceParameter2 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name3",
                "domain_value2").done();
        final SDataSourceParameter dataSourceParameter3 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name2",
                "domain_value3").done();
        final SDataSourceParameter dataSourceParameter4 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name4",
                "domain_value4").done();
        final SDataSourceParameter dataSourceParameter5 = dataSourceParameterModelBuilder.createNewInstance(dataSource.getId(), "domain_name5",
                "domain_value5").done();
        final List<SDataSourceParameter> dataSourceParameterList = new ArrayList<SDataSourceParameter>();
        dataSourceParameterList.add(dataSourceParameter1);
        dataSourceParameterList.add(dataSourceParameter2);
        dataSourceParameterList.add(dataSourceParameter3);
        dataSourceParameterList.add(dataSourceParameter4);
        dataSourceParameterList.add(dataSourceParameter5);
        dataService.createDataSourceParameters(dataSourceParameterList);
        getTransactionService().complete();

        getTransactionService().begin();

        final Collection<SDataSourceParameter> collection1 = dataService
                .getDataSourceParameters(dataSource.getId(), QueryOptions.allResultsQueryOptions());
        assertEquals(5, collection1.size());

        final List<OrderByOption> orderByOptionList = new ArrayList<OrderByOption>();
        orderByOptionList.add(new OrderByOption(SDataSourceParameter.class, dataSourceParameterModelBuilder.getNameKey(), OrderByType.ASC));
        orderByOptionList.add(new OrderByOption(SDataSourceParameter.class, dataSourceParameterModelBuilder.getValueKey(), OrderByType.DESC));
        final QueryOptions queryQptions2 = new QueryOptions(orderByOptionList);
        final Collection<SDataSourceParameter> collection2 = dataService.getDataSourceParameters(dataSource.getId(), queryQptions2);
        assertEquals(5, collection2.size());

        final QueryOptions queryQptions3 = new QueryOptions(1, 10);
        final Collection<SDataSourceParameter> collection3 = dataService.getDataSourceParameters(dataSource.getId(), queryQptions3);
        assertEquals(4, collection3.size());

        final QueryOptions queryQptions4 = new QueryOptions(SDataSourceParameter.class, "name", OrderByType.ASC);
        final Collection<SDataSourceParameter> collection4 = dataService.getDataSourceParameters(dataSource.getId(), queryQptions4);
        assertEquals(5, collection4.size());

        final QueryOptions queryQptions5 = QueryOptions.getNextPage(new QueryOptions(0, 2));
        final Collection<SDataSourceParameter> collection5 = dataService.getDataSourceParameters(dataSource.getId(), queryQptions5);
        assertEquals(2, collection5.size());

        dataService.removeDataSourceParameters(dataSource.getId());
        dataService.removeDataSource(dataSource);
        getTransactionService().complete();
    }
}
