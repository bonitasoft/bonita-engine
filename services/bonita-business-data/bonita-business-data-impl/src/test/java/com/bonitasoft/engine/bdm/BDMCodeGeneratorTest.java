package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.Version;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

public class BDMCodeGeneratorTest extends CompilableCode {

    private BDMCodeGenerator bdmCodeGenerator;

    private File destDir;

    @Before
    public void setUp() throws Exception {
        bdmCodeGenerator = new BDMCodeGenerator(new BusinessObjectModel());
        destDir = new File(System.getProperty("java.io.tmpdir"), "generationDir");
        destDir.mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        destDir.delete();
    }

    @Test
    public void shouldbuildAstFromBom_FillModel() throws Exception {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("Employee");
        bom.addBusinessObject(employeeBO);
        bdmCodeGenerator = new BDMCodeGenerator(bom);
        bdmCodeGenerator.buildASTFromBom();
        assertThat(bdmCodeGenerator.getModel()._getClass("Employee")).isNotNull();
    }

    @Test
    public void shouldAddEntity_CreateAValidEnityFromBusinessObject() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
        bdmCodeGenerator.addEntity(employeeBO);
        final JDefinedClass definedClass = bdmCodeGenerator.getModel()._getClass(employeeBO.getQualifiedName());
        assertThat(definedClass).isNotNull();
        assertThat(definedClass._package().name()).isEqualTo("org.bonitasoft.hr");
        assertThat(definedClass._implements()).hasSize(2);
        final Iterator<JClass> it = definedClass._implements();
        JClass jClass = it.next();
        assertThat(jClass.fullName()).isEqualTo(Serializable.class.getName());
        jClass = it.next();
        assertThat(jClass.fullName()).isEqualTo(com.bonitasoft.engine.bdm.Entity.class.getName());
        assertThat(definedClass.annotations()).hasSize(1);
        final JAnnotationUse entityAnnotation = definedClass.annotations().iterator().next();
        assertThat(entityAnnotation.getAnnotationClass().fullName()).isEqualTo(Entity.class.getName());
        assertThat(entityAnnotation.getAnnotationMembers()).hasSize(1);

        assertThat(definedClass.getMethod("equals", new JType[] { definedClass.owner().ref(Object.class) })).isNotNull();
        assertThat(definedClass.getMethod("hashCode", new JType[] {})).isNotNull();

        final File sourceFileToCompile = new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "hr" + File.separatorChar
                + "Employee.java");
        sourceFileToCompile.delete();
        bdmCodeGenerator.generate(destDir);
        assertCompilationSuccessful(sourceFileToCompile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddEntity_ThrowAnIllegalArgumentException() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("java.lang.String");
        bdmCodeGenerator.addEntity(employeeBO);
    }

    @Test
    public void shouldAddColumnField_CreatePrimitiveAttribute_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
        final Field nameField = new Field();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        final JDefinedClass definedClass = bdmCodeGenerator.addClass("org.bonitasoft.hr.Employee");
        bdmCodeGenerator.addBasicField(definedClass, nameField);

        final JFieldVar nameFieldVar = definedClass.fields().get("name");
        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(bdmCodeGenerator.getModel().ref(String.class.getName()));
        assertThat(nameFieldVar.annotations()).hasSize(1);
        final JAnnotationUse annotationUse = nameFieldVar.annotations().iterator().next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Column.class.getName());

        final String name = getAnnotationParamValue(annotationUse, "name");
        assertThat(name).isNotNull().isEqualTo("NAME");
        final String nullable = getAnnotationParamValue(annotationUse, "nullable");
        assertThat(nullable).isNotNull().isEqualTo("true");

        final File sourceFileToCompile = new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "hr" + File.separatorChar
                + "Employee.java");
        sourceFileToCompile.delete();
        bdmCodeGenerator.generate(destDir);
        assertCompilationSuccessful(sourceFileToCompile);
    }

    private String getAnnotationParamValue(final JAnnotationUse annotationUse, final String paramName) {
        final Map<String, JAnnotationValue> annotationParams = annotationUse.getAnnotationMembers();
        final JAnnotationValue nullableValue = annotationParams.get(paramName);
        final StringWriter writer = new StringWriter();
        nullableValue.generate(new JFormatter(writer));
        return writer.toString().replace("\"", "");
    }

    @Test
    public void shouldAddBasicField_AddAFieldWithTemporalAnnotation_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
        final Field nameField = new Field();
        nameField.setName("name");
        nameField.setType(FieldType.DATE);
        nameField.setNullable(Boolean.FALSE);
        final JDefinedClass definedClass = bdmCodeGenerator.addClass("org.bonitasoft.hr.Employee");
        bdmCodeGenerator.addBasicField(definedClass, nameField);

        final JFieldVar nameFieldVar = definedClass.fields().get("name");
        assertThat(nameFieldVar).isNotNull();
        assertThat(nameFieldVar.type()).isEqualTo(bdmCodeGenerator.getModel().ref(Date.class.getName()));
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

        final File sourceFileToCompile = new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "hr" + File.separatorChar
                + "Employee.java");
        sourceFileToCompile.delete();
        bdmCodeGenerator.generate(destDir);
        assertCompilationSuccessful(sourceFileToCompile);
    }

    @Test
    public void shouldAddAccessors_AddAccessorMethods_InDefinedClass() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
        final Field nameField = new Field();
        nameField.setName("name");
        nameField.setType(FieldType.STRING);
        final JDefinedClass definedClass = bdmCodeGenerator.addClass("org.bonitasoft.hr.Employee");
        final JFieldVar basicField = bdmCodeGenerator.addBasicField(definedClass, nameField);

        bdmCodeGenerator.addAccessors(definedClass, basicField);

        assertThat(definedClass.methods()).hasSize(2);
        final JMethod setter = (JMethod) definedClass.methods().toArray()[0];
        assertThat(setter.name()).isEqualTo("setName");

        final JMethod getter = (JMethod) definedClass.methods().toArray()[1];
        assertThat(getter.name()).isEqualTo("getName");

        final File sourceFileToCompile = new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "hr" + File.separatorChar
                + "Employee.java");
        sourceFileToCompile.delete();
        bdmCodeGenerator.generate(destDir);
        assertCompilationSuccessful(sourceFileToCompile);
    }

    @Test
    public void shouldToJavaType_ReturnIntegerClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.INTEGER).name()).isEqualTo(Integer.class.getSimpleName());
    }

    @Test
    public void shouldToJavaType_ReturnStringClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.STRING).name()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void shouldToJavaType_ReturnLongClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.LONG).name()).isEqualTo(Long.class.getSimpleName());
    }

    @Test
    public void shouldToJavaType_ReturnDoubleClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.DOUBLE).name()).isEqualTo(Double.class.getSimpleName());
    }

    @Test
    public void shouldToJavaType_ReturnFloatClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.FLOAT).name()).isEqualTo(Float.class.getSimpleName());
    }

    @Test
    public void shouldToJavaType_ReturnBooleanClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.BOOLEAN).name()).isEqualTo(Boolean.class.getSimpleName());
    }

    @Test
    public void shouldToJavaType_ReturnDateClass() throws Exception {
        assertThat(bdmCodeGenerator.toJavaType(FieldType.DATE).name()).isEqualTo(Date.class.getSimpleName());
    }

    @Test
    public void shouldAddPersistenceIdFieldAndAccessors_AddPersistenceId() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
        final JDefinedClass definedClass = bdmCodeGenerator.addClass("org.bonitasoft.hr.Employee");
        bdmCodeGenerator.addPersistenceIdFieldAndAccessors(definedClass);

        final JFieldVar idFieldVar = definedClass.fields().get(Field.PERSISTENCE_ID);
        assertThat(idFieldVar).isNotNull();
        assertThat(idFieldVar.type()).isEqualTo(bdmCodeGenerator.getModel().ref(Long.class.getName()));
        assertThat(idFieldVar.annotations()).hasSize(2);
        final Iterator<JAnnotationUse> iterator = idFieldVar.annotations().iterator();
        JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Id.class.getName());
        annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(GeneratedValue.class.getName());

        final File sourceFileToCompile = new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "hr" + File.separatorChar
                + "Employee.java");
        sourceFileToCompile.delete();
        bdmCodeGenerator.generate(destDir);
        assertCompilationSuccessful(sourceFileToCompile);
    }

    @Test
    public void shouldAddPersistenceVersionFieldAndAccessors_AddPersistenceVersion() throws Exception {
        final BusinessObject employeeBO = new BusinessObject();
        employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
        final JDefinedClass definedClass = bdmCodeGenerator.addClass("org.bonitasoft.hr.Employee");
        bdmCodeGenerator.addPersistenceVersionFieldAndAccessors(definedClass);

        final JFieldVar versionFieldVar = definedClass.fields().get(Field.PERSISTENCE_VERSION);
        assertThat(versionFieldVar).isNotNull();
        assertThat(versionFieldVar.type()).isEqualTo(bdmCodeGenerator.getModel().ref(Long.class.getName()));
        assertThat(versionFieldVar.annotations()).hasSize(1);
        final Iterator<JAnnotationUse> iterator = versionFieldVar.annotations().iterator();
        final JAnnotationUse annotationUse = iterator.next();
        assertThat(annotationUse.getAnnotationClass().fullName()).isEqualTo(Version.class.getName());

        final File sourceFileToCompile = new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "hr" + File.separatorChar
                + "Employee.java");
        sourceFileToCompile.delete();
        bdmCodeGenerator.generate(destDir);
        assertCompilationSuccessful(sourceFileToCompile);
    }

}
