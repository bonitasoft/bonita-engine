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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ClassReflectorTest {

    private static final String NOT_A_METHOD = "not a method";

    private static final String NOT_A_CLASS = "not a class";

    private Pojo pojo;

    @BeforeEach
    void before() {
        pojo = new Pojo();
    }

    @Test
    void testGetGetterName() {
        // can't work with boolean since field name type is unknown
        assertThat(ClassReflector.getGetterName("longs")).isEqualTo("getLongs");
        assertThat(ClassReflector.getGetterName("bigChoices")).isEqualTo("getBigChoices");

    }

    @Test
    void testGetGetterMethod() {
        assertThat(ClassReflector.getGetterName("bigChoice", Boolean.class)).isEqualTo("isBigChoice");
        assertThat(ClassReflector.getGetterName("longs", Long.class)).isEqualTo("getLongs");
        assertThat(ClassReflector.getGetterName("bigChoices", ArrayList.class)).isEqualTo("getBigChoices");

    }

    @Test
    void testGetterReturnType() throws Exception {
        assertThat(ClassReflector.getGetterReturnType(pojo.getClass(), "getDate")).isEqualTo(Date.class);
    }

    @Test
    void testGetAccessibleGetters() {
        final Collection<Method> accessibleGetters = ClassReflector.getAccessibleGetters(pojo.getClass());

        // isChoice, getDate, getClass, getLongs, getBigChoice, getBigChoices
        assertThat(accessibleGetters).hasSize(7);
    }

    @Test
    void testGetClass() throws Exception {
        final Class<? extends Pojo> class1 = ClassReflector.getClass(pojo.getClass(), pojo.getClass().getName());
        assertThat(class1).isEqualTo(pojo.getClass());
    }

    @Test
    void testGetClassException() {
        assertThrows(SReflectException.class, () -> ClassReflector.getClass(pojo.getClass(), NOT_A_CLASS));
    }

    @Test
    void testGetObject() throws Exception {
        assertThat(ClassReflector.getObject(pojo.getClass(), pojo.getClass().getName())).isNotNull();
    }

    @Test
    void testGetObjectException() {
        assertThrows(SReflectException.class, () -> ClassReflector.getObject(pojo.getClass(), NOT_A_CLASS));
    }

    @Test
    void testGetConstructor() throws Exception {
        ClassReflector.getConstructor(pojo.getClass(), pojo.getClass().getName());
    }

    @Test
    void testGetConstructorException() {
        assertThrows(SReflectException.class,
                () -> assertThat(ClassReflector.getConstructor(pojo.getClass(), NOT_A_CLASS)).isNotNull());
    }

    @Test
    void testGetConstructorNoClassName() throws Exception {
        final Constructor<? extends Pojo> constructor = ClassReflector.getConstructor(pojo.getClass());
        assertThat(constructor).isNotNull();
    }

    @Test
    void testGetConstructorNoClassNameException() {
        assertThrows(SReflectException.class, () -> ClassReflector.getConstructor(pojo.getClass(), String.class));
    }

    @Test
    void testGetInstance() throws Exception {
        ClassReflector.getInstance(ClassReflector.getConstructor(pojo.getClass()));
    }

    @Test
    void testGetInstanceException() {
        assertThrows(SReflectException.class,
                () -> ClassReflector.getInstance(ClassReflector.getConstructor(pojo.getClass()), String.class));
    }

    @Test
    void testInvokeGetter() throws Exception {
        final Date date = new Date();
        pojo.setDate(date);
        final Object invokeGetter = ClassReflector.invokeGetter(pojo, "getDate");
        assertThat(invokeGetter).isEqualTo(date);
    }

    @Test
    void testInvokeGetterException() {
        assertThrows(SReflectException.class, () -> ClassReflector.invokeGetter(pojo, NOT_A_METHOD));
    }

    @Test
    void testInvokeSetter() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeSetter(pojo, "setDate", date.getClass(), date);
        assertThat(pojo.getDate()).isEqualTo(date);
    }

    @Test
    void testInvokeSetterException() {
        assertThrows(SReflectException.class,
                () -> ClassReflector.invokeSetter(pojo, NOT_A_METHOD, Date.class, new Date()));
    }

    @Test
    void testGetMethod() throws Exception {
        final Method method = ClassReflector.getMethod(pojo.getClass(), "getDate");
        assertThat(method).isEqualTo(ClassReflector.getMethodByName(pojo.getClass(), "getDate"));
    }

    @Test
    void testInvokeMethodByName() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeMethodByName(pojo, "setDate", date);
        assertThat(pojo.getDate()).isEqualTo(date);
    }

    @Test
    void testInvokeMethodByName_should_throw_exception_on_method_name() {
        assertThrows(SReflectException.class, () -> ClassReflector.invokeMethodByName(pojo, NOT_A_METHOD, new Date()));
    }

    @Test
    void testInvokeMethodByName_should_throw_exception_on_bad_parameter() {
        assertThrows(SReflectException.class, () -> ClassReflector.invokeMethodByName(pojo, "setDate", "not a date"));
    }

    @Test
    void testInvokeMethod() throws Exception {
        final Date date = new Date();
        ClassReflector.invokeMethod(pojo, "setDate", Date.class, date);
        assertThat(pojo.getDate()).isEqualTo(date);
    }

    @Test
    void testInvokeMethodWithParams() throws Exception {
        final Class<?>[] parameterType = new Class<?>[] { String.class, Integer.class };
        final Object[] parameterValues = new Object[] { "string", 1 };

        final Object result = ClassReflector.invokeMethod(pojo, "twoParamMethod", parameterType, parameterValues);
        assertThat(result.toString()).isEqualTo("string*1");
    }

    @Test
    void testGetCompatibleMethod() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", Boolean.class);
        assertThat(compatibleMethod).isNotNull();

    }

    @Test
    void testGetCompatibleMethod_with_existing_method() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "isChoice");
        assertThat(compatibleMethod).isNotNull();

    }

    @Test
    void testGetCompatibleMethod_with_wrong_parameters_type() {
        assertThrows(SReflectException.class,
                () -> ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", String.class));
    }

    @Test
    void testGetCompatibleMethod_with_wrong_parameters_count() {
        assertThrows(SReflectException.class,
                () -> ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", String.class, Date.class));
    }

    @Test
    void testGetCompatibleMethod_with_wrong_null_parameters() {
        assertThrows(SReflectException.class,
                () -> ClassReflector.getCompatibleMethod(pojo.getClass(), "setChoice", null));
    }

    @Test
    void testGetCompatibleMethod_with_parameters() throws Exception {
        final Method compatibleMethod = ClassReflector.getCompatibleMethod(pojo.getClass(), "twoParamMethod", String.class, Integer.class);
        assertThat(compatibleMethod).isNotNull();
    }

    @Test
    void testGetGetterReturnType() throws Exception {
        final Type result = ClassReflector.getGetterReturnType(pojo.getClass(), "getDate");
        assertThat(result).isEqualTo(Date.class);
    }

    @Test
    void testGetGetterReturnType_should_throw_exception() {
        assertThrows(SReflectException.class, () -> ClassReflector.getGetterReturnType(pojo.getClass(), NOT_A_METHOD));
    }

    @Test
    void testGetDeclaredSetters() {
        final Method[] declaredSetters = ClassReflector.getDeclaredSetters(pojo.getClass());

        // setDate,setChoice,setLongs,setBigChoice,setBigChoices
        assertThat(declaredSetters).hasSize(6);

        for (final Method method : declaredSetters) {
            assertThat(ClassReflector.isAGetterMethod(method)).isFalse();
            assertThat(ClassReflector.isASetterMethod(method)).isTrue();
        }
    }

    @Test
    void testGetDeclaredGetters() {
        final Method[] declaredSetters = ClassReflector.getDeclaredGetters(pojo.getClass());

        // isChoice, getDate, getLongs, getBigChoice, getBigChoices
        assertThat(declaredSetters).hasSize(6);

        for (final Method method : declaredSetters) {
            assertThat(ClassReflector.isAGetterMethod(method)).isTrue();
            assertThat(ClassReflector.isASetterMethod(method)).isFalse();
        }
    }

    @Test
    void testGetFieldName() {
        assertThat(ClassReflector.getFieldName("isChoice")).isEqualTo("choice");
        assertThat(ClassReflector.getFieldName("getDate")).isEqualTo("date");
        assertThat(ClassReflector.getFieldName("get")).isEqualTo("");
    }

    @Test
    void testSetField_on_sub_object() throws Exception {
        Date date = new Date();
        pojo.setChild(new Pojo());
        ClassReflector.setField(pojo, "child.date", date);
        assertThat(pojo.getChild().getDate()).isEqualTo(date);
    }

    @Test
    void testSetField_on_object() throws Exception {
        Pojo parameterValue = new Pojo();
        ClassReflector.setField(pojo, "child", parameterValue);
        assertThat(pojo.getChild()).isEqualTo(parameterValue);
    }

}
