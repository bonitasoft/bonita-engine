package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.fail;
import static org.fest.assertions.Assertions.assertThat;

import javax.persistence.Entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;

public class BDMCodeGeneratorTest {

	private BDMCodeGenerator bdmCodeGenerator;

	@Before
	public void setUp() throws Exception {
		bdmCodeGenerator = new BDMCodeGenerator(new BusinessObjectModel());
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shouldbuildAstFromBom_FillModel() throws Exception {
		BusinessObjectModel bom = new BusinessObjectModel();
		BusinessObject employeeBO = new BusinessObject();
		employeeBO.setQualifiedName("Employee");
		bom.addBusinessObject(employeeBO);
		bdmCodeGenerator = new BDMCodeGenerator(bom);
		bdmCodeGenerator.buildASTFromBom();
		assertThat(bdmCodeGenerator.getModel()._getClass("Employee")).isNotNull();
	}
	
	@Test
	public void shouldAddEntity_CreateAValidEnityFromBusinessObject() throws Exception {
		BusinessObject employeeBO = new BusinessObject();
		employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
		bdmCodeGenerator.addEntity(employeeBO);
		JDefinedClass definedClass = bdmCodeGenerator.getModel()._getClass(employeeBO.getQualifiedName());
		assertThat(definedClass).isNotNull();
		assertThat(definedClass._package().name()).isEqualTo("org.bonitasoft.hr");
		assertThat(definedClass.annotations()).hasSize(1);
		JAnnotationUse entityAnnotation = definedClass.annotations().iterator().next();
		assertThat(entityAnnotation.getAnnotationClass().fullName()).isEqualTo(Entity.class.getName());
		assertThat(entityAnnotation.getAnnotationMembers()).hasSize(1);
	}
	
	@Test
	public void shouldAddFieldAndAccessors_CreatePrimitiveAttribute_AndAccessorMethods_InDefinedClass() throws Exception {
		BusinessObject employeeBO = new BusinessObject();
		employeeBO.setQualifiedName("org.bonitasoft.hr.Employee");
		Field nameField = new Field();
		nameField.setName("name");
		nameField.setType(FieldType.STRING);
		JDefinedClass definedClass = bdmCodeGenerator.addClass("org.bonitasoft.hr.Employee");
		bdmCodeGenerator.addFieldAndAccessors(definedClass, nameField);
	
		assertThat(definedClass.fields().get("name")).isNotNull();
		assertThat(definedClass.fields().get("name").type()).isEqualTo(bdmCodeGenerator.getModel().ref(String.class.getName()));
		
		assertThat(definedClass.methods()).hasSize(2);
		JMethod setter = (JMethod) definedClass.methods().toArray()[0];
		assertThat(setter.name()).isEqualTo("setName");
		
		JMethod getter = (JMethod) definedClass.methods().toArray()[1];
		assertThat(getter.name()).isEqualTo("getName");
		
		fail("check field annotation");
	}

}
