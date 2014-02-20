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

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

/**
 * @author Romain Bioteau
 *
 */
public class HashCodeBuilderTest {

	private CodeGenerator codeGenerator;
	private HashCodeBuilder hashCodeBuilder;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		codeGenerator = new CodeGenerator();
		hashCodeBuilder = new HashCodeBuilder();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shouldGenerate_AddHashCodeJMethodInDefinedClass() throws Exception {
		JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
		codeGenerator.addField(definedClass, "name", String.class);
		codeGenerator.addField(definedClass, "age", Integer.class);
		codeGenerator.addField(definedClass, "height", Float.class);
		codeGenerator.addField(definedClass, "isMarried", Boolean.class);
		codeGenerator.addField(definedClass, "timestamp", Long.class);
		codeGenerator.addField(definedClass, "weight", Double.class);
		codeGenerator.addField(definedClass, "returnDate",  codeGenerator.getModel().ref(Date.class));
		JMethod hashcodeMethod = hashCodeBuilder.generate(definedClass);
		assertThat(hashcodeMethod).isNotNull();
		assertThat(hashcodeMethod.name()).isEqualTo("hashCode");
		assertThat(hashcodeMethod.hasSignature(new JType[]{})).isTrue();
		assertThat(hashcodeMethod.type().fullName()).isEqualTo(int.class.getName());
		
		JBlock body = hashcodeMethod.body();
		assertThat(body).isNotNull();
		assertThat(body.getContents()).isNotEmpty();
	}

}
