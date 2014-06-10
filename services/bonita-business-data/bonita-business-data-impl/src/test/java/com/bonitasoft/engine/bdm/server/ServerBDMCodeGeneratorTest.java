package com.bonitasoft.engine.bdm.server;

import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectModelBuilder.aBOM;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aStringField;
import static com.bonitasoft.engine.bdm.model.builder.UniqueConstraintBuilder.aUniqueConstraint;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.sun.codemodel.JDefinedClass;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerBDMCodeGeneratorTest {

    private static final String EMPLOYEE_QUALIFIED_NAME = "org.bonitasoft.hr.Employee";

    private static final String SERVER_EMPLOYEE_DAO_IMPL_FILE = "org/bonitasoft/hr/server/EmployeeDAOImpl.java";

    private ServerBDMCodeGenerator serverBDMCodeGenerator;

    @Mock
    private BusinessObject bo;

    @Mock
    private JDefinedClass entity;

    private File destDir;

    @Before
    public void setUp() throws Exception {
        BusinessObjectModel bom = new BusinessObjectModel();
        serverBDMCodeGenerator = new ServerBDMCodeGenerator(bom);
        destDir = Files.newTemporaryFolder();
    }

    @After
    public void tearDown() throws Exception {
        Files.delete(destDir);
    }

    @Test
    public void toDaoImplClassnameShouldAddPointServerToLastPackagePart() throws Exception {
        BusinessObject myBo = new BusinessObject();
        myBo.setQualifiedName("com.bonitasoft.business.domain.Stool");

        String daoImplClassname = new ServerBDMCodeGenerator(new BusinessObjectModel()).toDaoImplClassname(myBo);

        assertThat(daoImplClassname).isEqualTo("com.bonitasoft.business.domain.server.StoolDAOImpl");
    }

    @Test
    public void toDaoImplClassnameShouldAddPointServerOnDefaultPackageBO() throws Exception {
        BusinessObject myBo = new BusinessObject();
        myBo.setQualifiedName("Zucchini");

        String daoImplClassname = new ServerBDMCodeGenerator(new BusinessObjectModel()).toDaoImplClassname(myBo);

        assertThat(daoImplClassname).isEqualTo("server.ZucchiniDAOImpl");
    }

    @Test
    public void serverDAOImplShouldGenerateConstructorWithBusinessDataRepository() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        final SimpleField aField = new SimpleField();
        aField.setName("aField");
        aField.setType(FieldType.STRING);
        employeeBO.getFields().add(aField);
        serverBDMCodeGenerator = new ServerBDMCodeGenerator(bom);
        serverBDMCodeGenerator.generate(destDir);

        final String serverDaoImplContent = readGeneratedServerDAOImpl();
        assertThat(serverDaoImplContent).contains("public EmployeeDAOImpl(BusinessDataRepository businessDataRepository)",
                "this.businessDataRepository = businessDataRepository;");
    }

    @Test
    public void serverDAOImplShouldGenerateDefaultFindMethod() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        final SimpleField aField = new SimpleField();
        aField.setName("aField");
        aField.setType(FieldType.STRING);
        employeeBO.getFields().add(aField);
        serverBDMCodeGenerator = new ServerBDMCodeGenerator(bom);
        serverBDMCodeGenerator.generate(destDir);

        final String serverDaoImplContent = readGeneratedServerDAOImpl();
        assertThat(serverDaoImplContent).contains("public List<Employee> find(int startIndex, int maxResults)",
                "return businessDataRepository.findListByNamedQuery(\"Employee.find\", Employee.class, queryParameters, startIndex, maxResults);",
                "catch (Exception e)");
    }

    @Test
    public void should_generate_methods_to_fetch_bizz_data_according_to_unique_constraints() throws Exception {
        final BusinessObject employeeBO = aBO(EMPLOYEE_QUALIFIED_NAME)
                .withField(aStringField("name").build())
                .withUniqueConstraint(aUniqueConstraint().withName("uniqueName").withFieldNames("name").build())
                .build();

        serverBDMCodeGenerator = new ServerBDMCodeGenerator(aBOM().withBO(employeeBO).build());
        serverBDMCodeGenerator.generate(destDir);

        final String serverDaoImplContent = readGeneratedServerDAOImpl();
        assertThat(serverDaoImplContent).contains("public org.bonitasoft.hr.Employee findByName(String name)");
    }

    @Test
    public void serverDAOImplShouldGenerate_FindListByNamedQuery_ForListOfBOReturningQuery() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        serverBDMCodeGenerator = new ServerBDMCodeGenerator(bom);
        serverBDMCodeGenerator.generate(destDir);

        final String serverDaoImplContent = readGeneratedServerDAOImpl();
        assertThat(serverDaoImplContent).contains("public List<Employee> findByName(String name, int startIndex, int maxResults)",
                "return businessDataRepository.findListByNamedQuery(\"Employee.findByName\", Employee.class, queryParameters, startIndex, maxResults);");
    }

    @Test
    public void serverDAOImplShouldGenerateGoodReturnTypeForCustomQuery() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);
        employeeBO.addQuery("howManyAreWe", "SELECT count(*) FROM Employee", Long.class.getName());

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        serverBDMCodeGenerator = new ServerBDMCodeGenerator(bom);
        serverBDMCodeGenerator.generate(destDir);

        final String serverDaoImplContent = readGeneratedServerDAOImpl();
        assertThat(serverDaoImplContent).contains("public Long howManyAreWe()",
                "return businessDataRepository.findByNamedQuery(\"Employee.howManyAreWe\", Long.class, queryParameters);");
    }

    private String readGeneratedServerDAOImpl() throws IOException {
        final File daoImplem = new File(destDir, SERVER_EMPLOYEE_DAO_IMPL_FILE);
        return FileUtils.readFileToString(daoImplem);
    }

}
