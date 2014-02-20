/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;

import org.fest.assertions.MapAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * @author Romain Bioteau
 *
 */
public class CodeGeneratorTest {

	private CodeGenerator codeGenerator;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		codeGenerator = new CodeGenerator();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shouldAddClass_AddAJDefinedClassInModel_AndReturnIt() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		assertThat(definedClass).isNotNull().isInstanceOf(JDefinedClass.class);
		assertThat(definedClass.name()).isEqualTo("Entity");
		assertThat(definedClass.fullName()).isEqualTo("org.bonitasoft.Entity");
		assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity")).isNotNull().isSameAs(definedClass);
	}

	@Test
	public void shouldAddPrivateField_FromClass_AddAJVarFieldInDefinedClass_AndReturnIt() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
		assertThat(privateField).isNotNull().isInstanceOf(JFieldVar.class);
		assertThat(privateField.name()).isEqualTo("name");
		assertThat(privateField.type().name()).isEqualTo(String.class.getSimpleName());
		assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").fields()).includes(MapAssert.entry("name", privateField));
		
	
	}
	
	@Test
	public void shouldAddPrivateField_FromJType_AddAJVarFieldInDefinedClass_AndReturnIt() throws Exception {
		JDefinedClass employeeClass = codeGenerator.addClass("org.bonitasoft.Employee");
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar privateField = codeGenerator.addField(definedClass, "employee", employeeClass);
		assertThat(privateField).isNotNull().isInstanceOf(JFieldVar.class);
		assertThat(privateField.name()).isEqualTo("employee");
		assertThat(privateField.type().name()).isEqualTo("Employee");
		assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").fields()).includes(MapAssert.entry("employee", privateField));
		
		JClass stringList = codeGenerator.getModel().ref(List.class).narrow(String.class);
		JFieldVar collectionField = codeGenerator.addField(definedClass, "skills", stringList);
		assertThat(collectionField).isNotNull();
		assertThat(collectionField.type().name()).isEqualTo(List.class.getSimpleName()+"<"+String.class.getSimpleName()+">");
	}
	
	
	
	@Test
	public void shouldAddAnnotation_AddAJAnnotation_AndReturnIt() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JAnnotationUse annotation = codeGenerator.addAnnotation(definedClass,Deprecated.class);
		assertThat(annotation).isNotNull().isInstanceOf(JAnnotationUse.class);
		assertThat(annotation.getAnnotationClass().fullName()).isEqualTo(Deprecated.class.getName());
	}
	
	@Test
	public void shouldAddSetter_AddAJMethodInDefinedClass_AndReturnIt() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
		JMethod setter = codeGenerator.addSetter(definedClass,privateField);
		assertThat(setter).isNotNull().isInstanceOf(JMethod.class);
		assertThat(setter.name()).isEqualTo("setName");
		assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").methods()).contains(setter);
	}
	
	@Test
	public void shouldAddGetter_AddAJMethodInDefinedClass_AndReturnIt() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
		JMethod setter = codeGenerator.addGetter(definedClass,privateField);
		assertThat(setter).isNotNull().isInstanceOf(JMethod.class);
		assertThat(setter.name()).isEqualTo("getName");
		assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").methods()).contains(setter);
	}
	
	
	
	@Test
	public void shouldCheckAnnotationTarget_IsValid() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		codeGenerator.addAnnotation(definedClass,Deprecated.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCheckAnnotationTarget_ThrowIllegalArgumentExceptionForType() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		codeGenerator.addAnnotation(definedClass,Basic.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCheckAnnotationTarget_ThrowIllegalArgumentExceptionForField() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
		codeGenerator.addAnnotation(privateField,Entity.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldCheckAnnotationTarget_ThrowIllegalArgumentExceptionForMethod() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
		codeGenerator.addAnnotation(privateField,Entity.class);
	}
	
	@Test
	public void shouldGenerate_CreatePojoFile() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		JFieldVar nameField = codeGenerator.addField(definedClass, "name", String.class);
		
		JClass stringList = codeGenerator.getModel().ref(List.class).narrow(String.class);
		JFieldVar skillField = codeGenerator.addField(definedClass, "skills", stringList);
		codeGenerator.addGetter(definedClass, nameField);
		codeGenerator.addSetter(definedClass, nameField);
		codeGenerator.addAnnotation(definedClass, Entity.class);
		
		codeGenerator.addGetter(definedClass, skillField);
		codeGenerator.addSetter(definedClass, skillField);
		
		File destDir = new File(System.getProperty("java.io.tmpdir"),"generatedPojo");
		destDir.mkdirs();
		destDir.deleteOnExit();
		codeGenerator.generate(destDir);
		File rootFolder = new File(destDir,"org"+File.separatorChar+"bonitasoft");
		assertThat(rootFolder.listFiles()).isNotEmpty().contains(new File(rootFolder,"Entity.java"));
	}
	

	
}
