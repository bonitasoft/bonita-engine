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
package org.bonitasoft.engine.business.data.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Version;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.hibernate.annotations.GenericGenerator;
import org.junit.Before;
import org.junit.Test;

public class EntityCodeGeneratorTest {

    private static final String EMPLOYEE_QUALIFIED_NAME = "org.bonitasoft.hr.Employee";
    private static final String UNIQUE_RELATION_QUALIFIED_NAME = "org.bonitasoft.hr.Unique";

    private EntityCodeGenerator entityCodeGenerator;

    private CodeGenerator codeGenerator;

    private BusinessObjectModel bom = new BusinessObjectModel();

    @Before
    public void setUp() {
        codeGenerator = new CodeGenerator();
        entityCodeGenerator = new EntityCodeGenerator(codeGenerator, bom);
    }

    @Test
    public void shouldAddEntity_CreateAValidEntityFromBusinessObject() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addEntity(employeeBO);
        final JDefinedClass definedClass = codeGenerator.getModel()._getClass(employeeBO.getQualifiedName());
        assertThat(definedClass).isNotNull();
        assertThat(definedClass._package().name()).isEqualTo("org.bonitasoft.hr");
        final Iterator<JClass> it = definedClass._implements();
        final JClass jClass = it.next();
        assertThat(jClass.fullName()).isEqualTo(org.bonitasoft.engine.bdm.Entity.class.getName());
        assertThat(definedClass.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = definedClass.annotations().iterator();
        final JAnnotationUse entityAnnotation = iterator.next();
        assertThat(entityAnnotation.getAnnotationClass().fullName()).isEqualTo(Entity.class.getName());
        assertThat(entityAnnotation.getAnnotationMembers()).hasSize(1);

        final JAnnotationUse tableAnnotation = iterator.next();
        assertThat(tableAnnotation.getAnnotationClass().fullName()).isEqualTo(Table.class.getName());
        assertThat(tableAnnotation.getAnnotationMembers()).hasSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddEntity_ThrowAnIllegalArgumentException() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("java.lang.String");
        entityCodeGenerator.addEntity(employeeBO);
    }

    @Test
    public void shouldAddNamedQueries_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        employeeBO.addQuery("getEmployees", "SELECT e FROM Employee e", List.class.getName());
        final JDefinedClass entity = entityCodeGenerator.addEntity(employeeBO);

        final JAnnotationUse namedQueriesAnnotation = getAnnotation(entity, NamedQueries.class.getName());
        assertThat(namedQueriesAnnotation).isNotNull();
        final Map<String, JAnnotationValue> annotationMembers = namedQueriesAnnotation.getAnnotationMembers();
        assertThat(annotationMembers).hasSize(1);
    }

    @Test
    public void shouldAddPersistenceIdFieldAndAccessors_AddPersistenceId() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "postgres");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar).isNotNull();
        assertThat(idFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Long.class.getName()));
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Id.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(GeneratedValue.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(GenericGenerator.class.getName());
    }

    @Test
    public void should_add_sequence_strategy_to_fields_with_generatedValue_in_h2() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "h2");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "generator")).isEqualTo("default_bonita_seq_generator");
    }

    @Test
    public void should_add_sequence_strategy_to_fields_with_generatedValue_in_postgres() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "postgres");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "generator")).isEqualTo("default_bonita_seq_generator");
    }

    @Test
    public void should_add_sequence_strategy_to_fields_with_generatedValue_in_oracle() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "oracle");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(3);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "generator")).isEqualTo("default_bonita_seq_generator");
    }

    @Test
    public void should_add_identity_strategy_to_fields_with_generatedValue_in_mysql() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "mysql");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "strategy"))
                .isEqualTo("javax.persistence.GenerationType.IDENTITY");
    }

    @Test
    public void should_add_identity_strategy_to_fields_with_generatedValue_in_SQLServer() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass, "sqlserver");

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        iterator.next();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(getAnnotationParamValue(annotationUse, "strategy"))
                .isEqualTo("javax.persistence.GenerationType.IDENTITY");
    }

    @Test
    public void shouldAddAccessors_AddAccessorMethods_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        final JFieldVar basicField = entityCodeGenerator.addField(definedClass, nameField);

        entityCodeGenerator.addAccessors(definedClass, basicField);

        assertThat(definedClass.methods()).hasSize(2);
        final JMethod setter = (JMethod) definedClass.methods().toArray()[0];
        assertThat(setter.name()).isEqualTo("setName");

        final JMethod getter = (JMethod) definedClass.methods().toArray()[1];
        assertThat(getter.name()).isEqualTo("getName");
    }

    @Test
    public void shouldAddBasicField_AddAFieldWithTemporalAnnotation_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.DATE);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField);

        final JFieldVar nameFieldVar = definedClass.fields().get("name");
        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Date.class.getName()));
        assertThat(nameFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");

        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Temporal.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "value");
        assertThat(value).isNotNull().isEqualTo("javax.persistence.TemporalType.TIMESTAMP");
    }

    private String getAnnotationParamValue(final JAnnotationUse annotationUse, final String paramName) {
        final Map<String, JAnnotationValue> annotationParams = annotationUse.getAnnotationMembers();
        final JAnnotationValue nullableValue = annotationParams.get(paramName);
        final StringWriter writer = new StringWriter();
        nullableValue.generate(new JFormatter(writer));
        return writer.toString().replace("\"", "");
    }

    @Test
    public void shouldAddBooleanAccessors_AddAccessorMethods_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField foundField = new SimpleField();
        foundField.setName("found");
        foundField.setType(FieldType.BOOLEAN);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        final JFieldVar basicField = entityCodeGenerator.addField(definedClass, foundField);

        entityCodeGenerator.addAccessors(definedClass, basicField);

        assertThat(definedClass.methods()).hasSize(2);
        final JMethod setter = (JMethod) definedClass.methods().toArray()[0];
        assertThat(setter.name()).isEqualTo("setFound");

        final JMethod getter = (JMethod) definedClass.methods().toArray()[1];
        assertThat(getter.name()).isEqualTo("isFound");
    }

    @Test
    public void shouldAddColumnField_CreatePrimitiveAttribute_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        nameField.setLength(Integer.valueOf(45));
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField);

        final JFieldVar nameFieldVar = definedClass.fields().get("name");
        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(String.class.getName()));
        assertThat(nameFieldVar.annotations()).hasSize(1);
        final JAnnotationUse annotationUse = nameFieldVar.annotations().iterator().next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("true");
        final String length = getAnnotationParamValue(annotationUse, "length");
        assertThat(length).isNotNull().isEqualTo("45");
    }

    @Test
    public void shouldAddColumnField() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final SimpleField nameField = new SimpleField();
        nameField.setName("description");
        nameField.setType(FieldType.TEXT);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);

        entityCodeGenerator.addField(definedClass, nameField);

        final JFieldVar nameFieldVar = definedClass.fields().get("description");
        assertTextField(nameFieldVar);
    }

    private void assertTextField(final JFieldVar fieldVar) {
        final Collection<JAnnotationUse> annotations = fieldVar.annotations();
        assertThat(annotations).hasSize(2);
        final Iterator<JAnnotationUse> iterator = annotations.iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Lob.class.getName());
    }

    @Test
    public void shouldAddPersistenceVersionFieldAndAccessors_AddPersistenceVersion() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceVersionFieldAndAccessors(definedClass);

        final JFieldVar versionFieldVar = definedClass.fields().get(Field.PERSISTENCE_VERSION);
        assertThat(versionFieldVar).isNotNull();
        assertThat(versionFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Long.class.getName()));
        assertThat(versionFieldVar.annotations()).hasSize(1);
        final Iterator<JAnnotationUse> iterator = versionFieldVar.annotations().iterator();
        final JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Version.class.getName());
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
    public void should_add_real_columnName_in_unique_key() throws Exception {
        //given
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);

        final BusinessObject uniqueRelationBO = new BusinessObject();
        uniqueRelationBO.setQualifiedName(UNIQUE_RELATION_QUALIFIED_NAME);

        final RelationField relationField = new RelationField();
        relationField.setName("uniqueRelation");
        relationField.setReference(uniqueRelationBO);
        employeeBO.addField(relationField);
        employeeBO.addUniqueConstraint("unique_constraint", "uniqueRelation");

        //when
        final JDefinedClass jDefinedClass = entityCodeGenerator.addEntity(employeeBO);

        //then
        final Writer writer = new StringWriter();
        final JAnnotationUse tableAnnotation = getAnnotation(jDefinedClass, Table.class.getCanonicalName());
        assertThat(tableAnnotation).as("should have Table Annotation").isNotNull();
        tableAnnotation.generate(new JFormatter(writer));
        final String EOL = System.getProperty("line.separator");
        assertThat(writer.toString()).as("should rename relation field to real column name")
                .isEqualTo("@javax.persistence.Table(name = \"EMPLOYEE\", uniqueConstraints = {" + EOL +
                        "    @javax.persistence.UniqueConstraint(name = \"UNIQUE_CONSTRAINT\", columnNames = {" + EOL +
                        "        \"UNIQUERELATION_PID\"" + EOL +
                        "    })" + EOL +
                        "})");
    }

    @Test
    public void annotateSimpleField_should_generate_the_correct_annotation_for_localDate_type()
            throws JClassAlreadyExistsException {

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.LOCALDATE);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField);
        final JFieldVar nameFieldVar = definedClass.fields().get("name");

        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(LocalDate.class.getName()));
        assertThat(nameFieldVar.annotations().size()).isEqualTo(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());
        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");
        final int length = Integer.parseInt(getAnnotationParamValue(annotationUse, "length"));
        assertThat(length).isEqualTo(10);
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Convert.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "converter");
        assertThat(value).isNotNull().isEqualTo("org.bonitasoft.engine.business.data.generator.DateConverter.class");

    }

    @Test
    public void annotateSimpleField_should_generate_the_correct_annotation_for_localDateAndTime_type()
            throws JClassAlreadyExistsException {

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        SimpleField nameField = new SimpleField();
        nameField.setName("name");
        nameField.setType(FieldType.LOCALDATETIME);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField);
        final JFieldVar nameFieldVar = definedClass.fields().get("name");

        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(LocalDateTime.class.getName()));
        assertThat(nameFieldVar.annotations().size()).isEqualTo(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");
        final int length = Integer.parseInt(getAnnotationParamValue(annotationUse, "length"));
        assertThat(length).isEqualTo(30);
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Convert.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "converter");
        assertThat(value).isNotNull()
                .isEqualTo("org.bonitasoft.engine.business.data.generator.DateAndTimeConverter.class");
    }

    @Test
    public void annotateSimpleField_should_generate_the_correct_annotation_for_OffsetDateAndTime_type()
            throws JClassAlreadyExistsException {

        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        SimpleField nameField = new SimpleField();
        nameField.setName("reunion");
        nameField.setType(FieldType.OFFSETDATETIME);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addField(definedClass, nameField);
        final JFieldVar nameFieldVar = definedClass.fields().get("reunion");

        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(OffsetDateTime.class.getName()));
        assertThat(nameFieldVar.annotations().size()).isEqualTo(2);
        final Iterator<JAnnotationUse> iterator = nameFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("REUNION");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("false");
        final int length = Integer.parseInt(getAnnotationParamValue(annotationUse, "length"));
        assertThat(length).isEqualTo(30);
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Convert.class.getName());
        assertThat(annotationUse.getAnnotationMembers()).hasSize(1);
        final String value = getAnnotationParamValue(annotationUse, "converter");
        assertThat(value).isNotNull()
                .isEqualTo("org.bonitasoft.engine.business.data.generator.OffsetDateTimeConverter.class");

    }
}
