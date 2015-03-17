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
package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Date;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

/**
 * @author Romain Bioteau
 * @author Celine Souchet
 */
public class EqualsBuilderTest extends CompilableCode {

    private EqualsBuilder equalsBuilder;

    private CodeGenerator codeGenerator;

    private File destDir;

    @Before
    public void setUp() {
        codeGenerator = new CodeGenerator();
        equalsBuilder = new EqualsBuilder();
        destDir = IOUtil.createTempDirectoryInDefaultTempDirectory("generationDir");
    }

    @After
    public void tearDown() throws Exception {
        IOUtil.deleteDir(destDir);
    }

    @Test
    public void shouldGenerate_AddEqualsJMethodInDefinedClass() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addField(definedClass, "name", String.class);
        codeGenerator.addField(definedClass, "age", Integer.class);
        codeGenerator.addField(definedClass, "returnDate", codeGenerator.getModel().ref(Date.class));
        final JMethod equalsMethod = equalsBuilder.generate(definedClass);
        assertThat(equalsMethod).isNotNull();
        assertThat(equalsMethod.name()).isEqualTo("equals");
        assertThat(equalsMethod.hasSignature(new JType[] { codeGenerator.getModel().ref(Object.class.getName()) })).isTrue();
        assertThat(equalsMethod.type().fullName()).isEqualTo(boolean.class.getName());

        final JBlock body = equalsMethod.body();
        assertThat(body).isNotNull();
        assertThat(body.getContents()).isNotEmpty();

        codeGenerator.getModel().build(destDir);
        assertCompilationSuccessful(new File(destDir, "org" + File.separatorChar + "bonitasoft" + File.separatorChar + "Entity.java"));
    }

}
