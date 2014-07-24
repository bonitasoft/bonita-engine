package com.bonitasoft.engine.bdm;

import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.aStringField;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.anIntegerField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

public class AbstractBDMCodeGeneratorTest {

    @Test
    public void should_be_a_able_to_suffix_a_package_of_a_qualified_name() throws Exception {
        final String suffixedPackage = AbstractBDMCodeGenerator.suffixPackage("a.qualified.name.Object", "suffixed");
        assertThat(suffixedPackage).isEqualTo("a.qualified.name.suffixed.Object");
    }

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
