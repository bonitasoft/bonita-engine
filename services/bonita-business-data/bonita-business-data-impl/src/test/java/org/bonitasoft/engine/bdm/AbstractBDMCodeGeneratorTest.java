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
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aStringField;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.anIntegerField;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.junit.Test;

public class AbstractBDMCodeGeneratorTest {

    @Test
    public void should_createMethodForNewInstance_return_jmethod_with_valid_name_and_parameters() throws Exception {
        final AbstractBDMCodeGenerator abstractBDMCodeGenerator = mock(AbstractBDMCodeGenerator.class);
        when(abstractBDMCodeGenerator.createMethodForNewInstance(any(BusinessObject.class), any(JDefinedClass.class), any(JDefinedClass.class)))
        .thenCallRealMethod();
        when(abstractBDMCodeGenerator.addMethodSignature(any(JDefinedClass.class), anyString(), any(JType.class)))
        .thenCallRealMethod();
        when(abstractBDMCodeGenerator.getModel()).thenReturn(new JCodeModel());

        final BusinessObject businessObject = aBO("org.bonita.Employee").withField(aStringField("name").notNullable().build())
                .withField(anIntegerField("age").build()).build();

        final CodeGenerator codeGenerator = new CodeGenerator();

        final JMethod jMethod = abstractBDMCodeGenerator.createMethodForNewInstance(businessObject, codeGenerator.addClass("org.bonita.Employee"),
                codeGenerator.addInterface("org.bonita.EmployeeDAO"));
        assertThat(jMethod).isNotNull();
        assertThat(jMethod.name()).isEqualTo("newInstance");
        assertThat(jMethod.params()).hasSize(1);
    }

}
