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
package org.bonitasoft.engine.bdm.client;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aRelationField;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aStringField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Strings.concat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.assertj.core.util.FilesException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.AbstractBDMCodeGenerator;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.CompilableCode;
import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;

public class ClientBDMCodeGeneratorTest extends CompilableCode {

    private static final String EMPLOYEE_QUALIFIED_NAME = "com.company.hr.Employee";

    private AbstractBDMCodeGenerator bdmCodeGenerator;

    private File destDir;

    @Before
    public void setUp() {
        bdmCodeGenerator = new ClientBDMCodeGenerator();
        try {
            destDir = Files.newTemporaryFolder();
        } catch (final FilesException fe) {
            System.err.println("Seems we cannot create temporary folder. Retrying...");
            final String tempFileName = String.valueOf(UUID.randomUUID().getLeastSignificantBits());
            destDir = Files.newFolder(concat(Files.temporaryFolderPath(), tempFileName));
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(destDir);
    }

    @Test
    public void shouldToJavaClass_ReturnIntegerClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.INTEGER).name()).isEqualTo(Integer.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnStringClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.STRING).name()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnLongClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.LONG).name()).isEqualTo(Long.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnDoubleClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.DOUBLE).name()).isEqualTo(Double.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnFloatClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.FLOAT).name()).isEqualTo(Float.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnBooleanClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.BOOLEAN).name()).isEqualTo(Boolean.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnDateClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.DATE).name()).isEqualTo(Date.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnStringTextClass() {
        assertThat(bdmCodeGenerator.toJavaClass(FieldType.TEXT).name()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void should_AddDao_generate_Dao_interface_with_query_methods_signature() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);
        final String daoContent = readGeneratedDAOInterface();
        assertThat(daoContent).contains("public List<Employee> findByName(String name, int startIndex, int maxResults)");
    }

    @Test
    public void queryGenerationReturningListShouldAddPaginationParameters() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);
        final SimpleField ageField = new SimpleField();
        ageField.setName("age");
        ageField.setType(FieldType.INTEGER);
        employeeBO.getFields().add(ageField);

        final Query query = new Query("getEmployeesByNameAndAge", "SELECT e FROM Employee e WHERE e.name = :myName AND e.age = :miEdad", List.class.getName());
        query.addQueryParameter("miEdad", Integer.class.getName());
        query.addQueryParameter("myName", String.class.getName());
        employeeBO.getQueries().add(query);
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);
        final String daoContent = readGeneratedDAOInterface();

        assertThat(daoContent).contains("public List<Employee> getEmployeesByNameAndAge(Integer miEdad, String myName, int startIndex, int maxResults)");
    }

    protected String getQueryMethodSignature(final Query query, final String queryReturnType, final String businessObjectName, final boolean returnsList) {
        String signature = "public " + getSimpleClassName(queryReturnType) + "<" + getSimpleClassName(businessObjectName) + "> " + query.getName() + "(";
        boolean first = true;
        for (final QueryParameter param : query.getQueryParameters()) {
            signature = appendCommaIfNotFirstParam(signature, first);
            signature += getSimpleClassName(param.getClassName()) + " " + param.getName();
            first = false;
        }
        if (returnsList) {
            signature = appendCommaIfNotFirstParam(signature, first);
            signature += "int startIndex, int maxResults";
        }
        signature += ")";
        return signature;
    }

    protected String appendCommaIfNotFirstParam(final String signature, final boolean first) {
        String newSignature = signature;
        if (!first) {
            newSignature += ", ";
        }
        return newSignature;
    }

    private String getSimpleClassName(final String qualifedClassName) {
        return qualifedClassName.substring(qualifedClassName.lastIndexOf('.') + 1);
    }

    @Test
    public void should_AddDao_generate_Dao_interface_with_unique_constraint_methods_signature() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("firstName");
        nameField.setType(FieldType.STRING);

        final SimpleField lastnameField = new SimpleField();
        lastnameField.setName("lastName");
        lastnameField.setType(FieldType.STRING);
        employeeBO.getFields().add(nameField);
        employeeBO.getFields().add(lastnameField);

        employeeBO.addUniqueConstraint("TOTO", "firstName", "lastName");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);
    }

    private String readGeneratedDAOInterface() throws IOException {
        final File daoInterface = new File(destDir, EMPLOYEE_QUALIFIED_NAME.replace(".", File.separator) + "DAO.java");
        return FileUtils.readFileToString(daoInterface);
    }

    public JAnnotationUse getAnnotation(final JDefinedClass definedClass, final String annotationClassName) {
        final Iterator<JAnnotationUse> iterator = definedClass.annotations().iterator();
        JAnnotationUse annotation = null;
        while (annotation == null && iterator.hasNext()) {
            final JAnnotationUse next = iterator.next();
            if (next.getAnnotationClass().fullName().equals(annotationClassName)) {
                annotation = next;
            }
        }
        return annotation;
    }

    @Test
    public void addIndexAnnotation() throws Exception {
        final BusinessObjectModel model = new BusinessObjectModel();
        final SimpleField field = new SimpleField();
        field.setName("firstName");
        field.setType(FieldType.STRING);
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("Employee");
        employeeBO.addField(field);
        employeeBO.addIndex("IDX_1", "firstName, lastName");
        model.addBusinessObject(employeeBO);

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(model, destDir);

        assertFilesAreEqual("Employee.java", "Employee.java.txt");
    }

    @Test
    public void addSimpleReferenceWithComposition() throws Exception {
        final RelationField aggregation = aRelationField().withName("address").composition().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregation);

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);

        assertFilesAreEqual("Employee.java", "EmployeeSimpleComposition.java.txt");
    }

    @Test
    public void addListReferenceWithComposition() throws Exception {
        final RelationField eager = aRelationField().withName("addresses").composition().multiple().referencing(addressBO()).build();
        final RelationField lazy = aRelationField().withName("skills").composition().multiple().lazy().referencing(skillBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(eager, lazy);

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);

        assertFilesAreEqual("Employee.java", "EmployeeListComposition.java.txt");
        assertFilesAreEqual("Skill.java", "Skill.java.txt");
    }

    @Test
    public void newInstance_is_generated_with_mandatory_fields_in_parameters() throws Exception {
        final InputStream resourceAsStream = ClientBDMCodeGeneratorTest.class.getResourceAsStream("/failing_bdm.zip");
        final BusinessObjectModel businessObjectModel = new BusinessObjectModelConverter().unzip(IOUtil.getAllContentFrom(resourceAsStream));

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(businessObjectModel, destDir);

        assertFilesAreEqual("com/test/model/PersonneDAOImpl.java", "PersonneDAOImpl.java.txt");
    }

    @Test
    public void addSimpleReferenceWithAggregation() throws Exception {
        final RelationField aggregation = aRelationField().withName("address").aggregation().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregation);

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);

        assertFilesAreEqual("Employee.java", "EmployeeSimpleAggregation.java.txt");
    }

    @Test
    public void addListReferenceWithAggregation() throws Exception {
        final RelationField aggregationMultiple = aRelationField().withName("addresses").aggregation().multiple().referencing(addressBO()).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregationMultiple);

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);

        assertFilesAreEqual("Employee.java", "EmployeeListAggregation.java.txt");
    }

    @Test
    public void addListReferenceWithLazyAggregation() throws Exception {
        final BusinessObject addressBO = addressBO();
        addressBO.addUniqueConstraint("city", "city");
        final RelationField aggregationMultiple = aRelationField().withName("addresses").aggregation().multiple().lazy().referencing(addressBO).build();
        final BusinessObjectModel bom = employeeWithRelations(aggregationMultiple);

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(bom, destDir);

        assertFilesAreEqual("AddressDAO.java", "AddressDAOWithLazyReferenceOnEmployee.java.txt");
        assertFilesAreEqual("AddressDAOImpl.java", "AddressDAOImplWithLazyReferenceOnEmployee.java.txt");
    }

    @Test
    public void addList() throws Exception {
        final BusinessObjectModel model = build();

        bdmCodeGenerator = new ClientBDMCodeGenerator();
        bdmCodeGenerator.generateBom(model, destDir);

        assertFilesAreEqual("Forecast.java", "ForecastList.java.txt");
    }

    private BusinessObject skillBO() {
        return aBO("Skill").withField(aStringField("skill").build()).build();
    }

    private BusinessObjectModel build() {
        final SimpleField field = new SimpleField();
        field.setName("temperatures");
        field.setType(FieldType.DOUBLE);
        field.setCollection(Boolean.TRUE);

        final BusinessObject forecastBO = new BusinessObject();
        forecastBO.setQualifiedName("Forecast");
        forecastBO.addField(field);

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(forecastBO);
        return model;
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

    private BusinessObject employeeBO() {
        return aBO("Employee").withField(aStringField("firstName").build()).build();
    }

    private BusinessObject addressBO() {
        return aBO("Address").withField(aStringField("street").build()).withField(aStringField("city").build()).build();
    }

    private void assertFilesAreEqual(final String qualifiedName, final String resourceName) throws URISyntaxException, IOException {
        final File file = new File(destDir, qualifiedName);
        final URL resource = ClientBDMCodeGeneratorTest.class.getResource(resourceName);
        final File expected = new File(resource.toURI());

        assertThat(file).hasContentEqualTo(expected);
    }

}
