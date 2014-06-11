package com.bonitasoft.engine.bdm.client;

import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectModelBuilder.aBOM;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aDoubleField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aRelationField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aStringField;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import com.bonitasoft.engine.bdm.BusinessObjectModelValidationException;
import com.bonitasoft.engine.bdm.CompilableCode;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.builder.BusinessObjectModelBuilder;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.sun.codemodel.JClassAlreadyExistsException;

public class ClientBDMCodeGeneratorTest extends CompilableCode {

    private static final String FORECAST_OUTPUT_FILE = "org/bonita/weather/impl/Forecast.java";
    private static final String ADDRESS_OUTPUT_FILE = "org/bonita/hr/AddressDAO.java";
    private static final String EMPLOYEE_OUTPUT_FILE = "org/bonita/hr/impl/Employee.java";

    private static final String EMPLOYEE_QUALIFIED_NAME = "org.bonita.hr.Employee";

    private AbstractBDMCodeGenerator bdmCodeGenerator;

    private File destDir;

    @Before
    public void setUp() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        destDir = Files.newTemporaryFolder();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(destDir);
    }

    private BusinessObjectModel employeeWithRelations(final RelationField... field) {
        final BusinessObject employeeBO = employeeBO();
        BusinessObjectModelBuilder aBom = aBOM().withBO(employeeBO);
        for (final RelationField relationField : field) {
            employeeBO.addField(relationField);
            aBom = aBom.withBO(relationField.getReference());
        }
        return aBom.build();
    }

    private BusinessObject forecastBO() {
        return aBO("org.bonita.weather.Forecast").withField(aDoubleField("temperatures").multiple().build()).build();
    }

    private BusinessObject skillBO() {
        return aBO("org.bonita.hr.Skill").withField(aStringField("skill").build()).build();
    }

    private BusinessObject employeeBO() {
        return aBO(EMPLOYEE_QUALIFIED_NAME).withField(aStringField("firstName").build()).build();
    }

    private BusinessObject addressBO() {
        return aBO("org.bonita.hr.Address").withField(aStringField("street").build()).withField(aStringField("city").build()).build();
    }

    private void generateCodeFor(final BusinessObjectModel bom) throws IOException, JClassAlreadyExistsException, BusinessObjectModelValidationException,
            ClassNotFoundException {
        bdmCodeGenerator = new ClientBDMCodeGenerator(bom);
        bdmCodeGenerator.generate(destDir);
    }

    private void assertFilesAreEqual(final String qualifiedName, final String resourceName) throws URISyntaxException, IOException {
        final File file = new File(destDir, qualifiedName);
        final URL resource = ClientBDMCodeGeneratorTest.class.getResource(resourceName);
        final File expected = new File(resource.toURI());

        assertThat(file).hasContentEqualTo(expected);
    }

    private String daoContent() throws IOException {
        final File daoInterface = new File(destDir, EMPLOYEE_QUALIFIED_NAME.replace(".", File.separator) + "DAO.java");
        return FileUtils.readFileToString(daoInterface);
    }

    @Test
    public void shouldbuildAstFromBom_FillModel() throws Exception {
        final BusinessObject employeeBO = employeeBO();

        generateCodeFor(aBOM().withBO(employeeBO).build());

        assertThat(bdmCodeGenerator.getModel()._getClass("org.bonita.hr.impl.Employee")).isNotNull();
    }

    @Test
    public void should_AddDao_generate_Dao_interface_with_query_methods_signature() throws Exception {
        final BusinessObject employeeBO = aBO(EMPLOYEE_QUALIFIED_NAME).withField(aStringField("name").build()).build();

        generateCodeFor(aBOM().withBO(employeeBO).build());

        assertThat(daoContent()).contains("public List<Employee> findByName(String name, int startIndex, int maxResults)");
    }

    @Test
    public void queryGenerationReturningListShouldAddPaginationParameters() throws Exception {
        final Query query = new Query("getEmployeesByNameAndAge", "SELECT e FROM Employee e WHERE e.name = :myName AND e.age = :miEdad", List.class.getName());
        query.addQueryParameter("miEdad", Integer.class.getName());
        query.addQueryParameter("myName", String.class.getName());

        final BusinessObject employeeBO = employeeBO();
        employeeBO.getQueries().add(query);

        generateCodeFor(aBOM().withBO(employeeBO).build());

        assertThat(daoContent()).contains("public List<Employee> getEmployeesByNameAndAge(Integer miEdad, String myName, int startIndex, int maxResults)");
    }

    @Test
    public void addIndexAnnotation() throws Exception {
        final BusinessObject employeeBO = employeeBO();
        employeeBO.addIndex("IDX_1", "firstName, lastName");

        generateCodeFor(aBOM().withBO(employeeBO).build());

        assertFilesAreEqual(EMPLOYEE_OUTPUT_FILE, "Employee.java");
    }

    @Test
    public void addSimpleReferenceWithComposition() throws Exception {
        final RelationField aggregation = aRelationField().withName("address").composition().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregation);

        generateCodeFor(bom);

        assertFilesAreEqual(EMPLOYEE_OUTPUT_FILE, "EmployeeSimpleComposition.java");
    }

    @Test
    public void addListReferenceWithComposition() throws Exception {
        final RelationField eager = aRelationField().withName("addresses").composition().multiple().referencing(addressBO()).build();
        final RelationField lazy = aRelationField().withName("skills").composition().multiple().lazy().referencing(skillBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(eager, lazy);

        generateCodeFor(bom);

        assertFilesAreEqual(EMPLOYEE_OUTPUT_FILE, "EmployeeListComposition.java");
    }

    @Test
    public void addSimpleReferenceWithAggregation() throws Exception {
        final RelationField aggregation = aRelationField().withName("address").aggregation().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregation);

        generateCodeFor(bom);

        assertFilesAreEqual(EMPLOYEE_OUTPUT_FILE, "EmployeeSimpleAggregation.java");
    }

    @Test
    public void addListReferenceWithAggregation() throws Exception {
        final RelationField aggregationMultiple = aRelationField().withName("addresses").aggregation().multiple().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregationMultiple);

        generateCodeFor(bom);

        assertFilesAreEqual(EMPLOYEE_OUTPUT_FILE, "EmployeeListAggregation.java");
    }

    @Test
    public void addListReferenceWithLazyAggregation() throws Exception {
        final RelationField aggregationMultiple = aRelationField().withName("addresses").aggregation().multiple().lazy().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregationMultiple);

        generateCodeFor(bom);

        assertFilesAreEqual(ADDRESS_OUTPUT_FILE, "AddressDAOWithLazyReferenceOnEmployee.java");
    }

    @Test
    public void addList() throws Exception {
        final BusinessObject forecast = forecastBO();

        generateCodeFor(aBOM().withBO(forecast).build());

        assertFilesAreEqual(FORECAST_OUTPUT_FILE, "ForecastList.java");
    }

}
