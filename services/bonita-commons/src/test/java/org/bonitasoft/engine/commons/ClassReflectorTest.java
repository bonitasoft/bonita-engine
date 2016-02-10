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
package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassReflectorTest {

    private static final String NOT_A_METHOD = "not a method";

    private static final String NOT_A_CLASS = "not a class";

    private Pojo pojo;

    @Mock
    private List<Long> expected;

    @Before
    public void before() {
        pojo = new Pojo();
    }

    @Test
    public void testGetGetterName() throws Exception {
        // can't work with boolean since field name type is unknown
        assertThat(ClassReflector.getGetterName("longs")).isEqualTo("getLongs");
        assertThat(ClassReflector.getGetterName("bigChoices")).isEqualTo("getBigChoices");

    }

    @Test
    public void testGetGetterMethod() throws Exception {
        assertThat(ClassReflector.getGetterName("bigChoice", Boolean.class)).isEqualTo("isBigChoice");
        assertThat(ClassReflector.getGetterName("longs", Long.class)).isEqualTo("getLongs");
        assertThat(ClassReflector.getGetterName("bigChoices", new ArrayList<Boolean>().getClass())).isEqualTo("getBigChoices");

    }

    @Test
    public void testGetterReturnType() throws Exception {
        assertThat(ClassReflector.getGetterReturnType(pojo.getClass(), "getDate")).isEqualTo(Date.class);
    }

    @Test
    public void testGetAccessibleGetters() throws Exception {
        final Collection<Method> accessibleGetters = ClassReflector.getAccessibleGetters(pojo.getClass());

        // isChoice, getDate, getClass, getLongs, getBigChoice, getBigChoices
        assertThat(accessibleGetters).hasSize(7);
    }

    @Test
    public void testGetClass() throws Exception {
        final Class<? extends Pojo> class1 = ClassReflector.getClass(pojo.getClass(), pojo.getClass().getName());
        assertThat(class1).isEqualTo(pojo.getClass());
    }

    @Test(expected = SReflectException.class)
    public void testGetClassException() throws Exception {
        ClassReflector.getClass(pojo.getClass(), NOT_A_CLASS);
    }

    @Test
    public void testGetObject() throws Exception {
        assertThat(ClassReflector.getObject(pojo.getClass(), pojo.getClass().getName())).isNotNull();
    }

    @Test(expected = SReflectException.class)
    public void testGetObjectException() throws Exception {
        ClassReflector.getObject(pojo.getClass(), NOT_A_CLASS);
    }

    @Test
    public void testGetConstructor() throws Exception {
        ClassReflector.getConstructor(pojo.getClass(), pojo.getClass().getName());
    }

    @Test(expected = SReflectException.class)
    public void testGetConstructorException() throws Exception {
        assertThat(ClassReflector.getConstructor(pojo.getClass(), NOT_A_CLASS)).isNotNull();
    }

    @Test
    public void testGetConstructorNoClassName() throws Exception {
        final Constructor<? extends Pojo> constructor = ClassReflector.getConstructor(pojo.getClass());
        assertThat(constructor).isNotNull();
    }

    @Test(expected = SReflectException.class)
    public void testGetConstructorNoClassNameException() throws Exception {
        ClassReflector.getConstructor(pojo.getClass(), String.class);
    }

    @Test
    public void testGetInstance() throws Exception {
        ClassReflector.getInstance(ClassReflector.getConstructor(pojo.getClass()));
    }

    @Test(expected = SReflectException.class)
    public void testGetInstanceException() throws Exception {
        assertThat(ClassReflector.getInstance(ClassReflector.getConstructor(pojo.getClass()), String.class)).isNotNull();
    }

    @Test
    public void testInvokeGetter() throws Exception {
        final Date date = new Date();
        pojo.setDate(date);
        final Object invokeGetter = ClassReflector.invokeGetter(pojo, "getDate");
        assertThat(invokeGetter).isEqualTo(date);
    }

    @Test(expected = SReflectException.class)
    public void testInvokeGetterException() throws Exception {
        ClassReflector.invokeGetter(pojo, NOT_A_METHOD);

    }

    @Test
    public void testInvokeSetter() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeSetter(pojo, "setDate", date.getClass(), date);
        assertThat(pojo.getDate()).isEqualTo(date);
    }

    @Test(expected = SReflectException.class)
    public void testInvokeSetterException() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeSetter(pojo, NOT_A_METHOD, date.getClass(), date);
    }

    @Test
    public void testgetMethod() throws Exception {
        final Method method = ClassReflector.getMethod(pojo.getClass(), "getDate");
        assertThat(method).isEqualTo(ClassReflector.getMethodByName(pojo.getClass(), "getDate"));
    }

    @Test
    public void testInvokeMethodByName() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeMethodByName(pojo, "setDate", date);
        assertThat(pojo.getDate()).isEqualTo(date);
    }

    @Test(expected = SReflectException.class)
    public void testInvokeMethodByName_should_throw_exception_on_method_name() throws Exception {
        ClassReflector.invokeMethodByName(pojo, NOT_A_METHOD, new Date());
    }

    @Test(expected = SReflectException.class)
    public void testInvokeMethodByName_should_throw_exception_on_bad_parameter() throws Exception {
        ClassReflector.invokeMethodByName(pojo, "setDate", "not a date");
    }

    @Test
    public void testInvokeMethod() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeMethod(pojo, "setDate", Date.class, date);
        assertThat(pojo.getDate()).isEqualTo(date);
    }

    @Test
    public void testInvokeMethodWithParams() throws Exception {
        final Class<?>[] parameterType = new Class<?>[] { String.class, Integer.class };
        final Object[] parameterValues = new Object[] { "string", new Integer(1) };

        final Object result = ClassReflector.invokeMethod(pojo, "twoParamMethod", parameterType, parameterValues);
        assertThat(result.toString()).isEqualTo("string*1");
    }

    @Test
    public void testgetCompatibleMethod() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", Boolean.class);
        assertThat(compatibleMethod).isNotNull();

    }

    @Test
    public void testgetCompatibleMethod_with_existing_method() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "isChoice");
        assertThat(compatibleMethod).isNotNull();

    }

    @Test(expected = SReflectException.class)
    public void testgetCompatibleMethod_with_wrong_parameters_type() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", String.class);
        assertThat(compatibleMethod).isNotNull();
    }

    @Test(expected = SReflectException.class)
    public void testgetCompatibleMethod_with_wrong_parameters_count() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", String.class, Date.class);
        assertThat(compatibleMethod).isNotNull();
    }

    @Test(expected = SReflectException.class)
    public void testgetCompatibleMethod_with_wrong_null_parameters() throws Exception {
        ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", null);

    }

    @Test
    public void testgetCompatibleMethod_with_parameters() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "twoParamMethod", String.class, Integer.class);
        assertThat(compatibleMethod).isNotNull();

    }

    @Test
    public void testgetGetterReturnType() throws Exception {
        final Type result = ClassReflector.getGetterReturnType(pojo.getClass(), "getDate");
        assertThat(result).isEqualTo(Date.class);
    }

    @Test(expected = SReflectException.class)
    public void testgetGetterReturnType_shouldthrow_exception() throws Exception {
        ClassReflector.getGetterReturnType(pojo.getClass(), NOT_A_METHOD);

    }

    @Test
    public void testGetDeclaredSetters() throws Exception {
        final Method[] declaredSetters = ClassReflector.getDeclaredSetters(pojo.getClass());

        // setDate,setChoice,setLongs,setBigChoice,setBigChoices
        assertThat(declaredSetters).hasSize(6);

        for (final Method method : declaredSetters) {
            assertThat(ClassReflector.isAGetterMethod(method)).isFalse();
            assertThat(ClassReflector.isASetterMethod(method)).isTrue();

        }

    }

    @Test
    public void testGetDeclaredGetters() throws Exception {
        final Method[] declaredSetters = ClassReflector.getDeclaredGetters(pojo.getClass());

        // isChoice, getDate, getLongs, getBigChoice, getBigChoices
        assertThat(declaredSetters).hasSize(6);

        for (final Method method : declaredSetters) {
            assertThat(ClassReflector.isAGetterMethod(method)).isTrue();
            assertThat(ClassReflector.isASetterMethod(method)).isFalse();

        }
    }

    @Test
    public void testGetFieldName() throws Exception {
        assertThat(ClassReflector.getFieldName("isChoice")).isEqualTo("choice");
        assertThat(ClassReflector.getFieldName("getDate")).isEqualTo("date");
        assertThat(ClassReflector.getFieldName("get")).isEqualTo("");

    }

    @Test
    public void testSetField_on_sub_object() throws Exception {
        Date date = new Date();
        pojo.setChild(new Pojo());
        ClassReflector.setField(pojo, "child.date", date);
        assertThat(pojo.getChild().getDate()).isEqualTo(date);
    }

    @Test
    public void testSetField_on_object() throws Exception {
        Pojo parameterValue = new Pojo();
        ClassReflector.setField(pojo, "child", parameterValue);
        assertThat(pojo.getChild()).isEqualTo(parameterValue);
    }

}
