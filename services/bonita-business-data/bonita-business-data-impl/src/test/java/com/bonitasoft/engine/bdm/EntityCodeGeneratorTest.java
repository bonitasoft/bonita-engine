package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.Version;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;

public class EntityCodeGeneratorTest {

    private static final String EMPLOYEE_QUALIFIED_NAME = "org.bonitasoft.hr.Employee";

    private EntityCodeGenerator entityCodeGenerator;

    private CodeGenerator codeGenerator;

    @Before
    public void setUp() {
        codeGenerator = new CodeGenerator();
        entityCodeGenerator = new EntityCodeGenerator(codeGenerator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_validate_that_classes_with_same_name_are_not_loaded_by_classloader() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("java.lang.String");

        entityCodeGenerator.addEntity(employeeBO);
    }

    @Test
    public void shouldAddPersistenceIdFieldAndAccessors_AddPersistenceId() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        final JDefinedClass definedClass = codeGenerator.addClass(EMPLOYEE_QUALIFIED_NAME);
        entityCodeGenerator.addPersistenceIdField(definedClass);

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar).isNotNull();
        assertThat(idFieldVar.type()).isEqualTo(codeGenerator.getModel().ref(Long.class.getName()));
        assertThat(idFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Id.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(GeneratedValue.class.getName());
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
        entityCodeGenerator.addPersistenceVersionField(definedClass);

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
}
