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

package org.bonitasoft.engine.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayInputStream;

import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderEnvironmentTest {

    @Mock
    public ClassLoader classloader;

    public ClassLoaderEnvironment classLoaderEnvironment;

    @Before
    public void before(){
        classLoaderEnvironment = new ClassLoaderEnvironment(classloader);
    }


    @Test
    public void should_toPointedNotation_return_the_packageName(){
        String packageName = classLoaderEnvironment.toPointedNotation(new char[][]{{'o', 'r', 'g'}, {'b', 'o', 'n', 'i', 't', 'a', 's', 'o', 'f', 't'}});
        assertThat(packageName).isEqualTo("org.bonitasoft");
    }

    @Test
    public void should_isPackage_return_false_for_known_class() throws ClassNotFoundException {
        doReturn(String.class).when(classloader).loadClass(String.class.getName());

        boolean aPackage = classLoaderEnvironment.isPackage(new char[][]{"java".toCharArray(), "lang".toCharArray()}, "String".toCharArray());

        assertThat(aPackage).isFalse();
    }

    @Test
    public void should_isPackage_return_false_for_parent_known_class() throws ClassNotFoundException {
        doReturn(String.class).when(classloader).loadClass(String.class.getName());

        boolean aPackage = classLoaderEnvironment.isPackage(charsArray("java.lang.String"),"SubElement".toCharArray());

        assertThat(aPackage).isFalse();
    }

    char[][] charsArray(String packageString) {
        String[] split = packageString.split("\\.");
        char[][] chars = new char[split.length][];
        for (int i = 0; i < split.length; i++) {
            chars[i] = split[i].toCharArray();
        }
        return chars;
    }


    @Test
    public void should_isPackage_return_true_for_package() throws ClassNotFoundException {
        doThrow(ClassNotFoundException.class).when(classloader).loadClass("java.lang");

        boolean aPackage = classLoaderEnvironment.isPackage(new char[][]{"java".toCharArray()}, "lang".toCharArray());

        assertThat(aPackage).isTrue();
    }

    @Test
    public void should_isPackage_return_false_for_empty_package() throws ClassNotFoundException {
        doReturn(this.getClass()).when(classloader).loadClass("Toto");

        boolean aPackage = classLoaderEnvironment.isPackage(new char[][]{}, "Toto".toCharArray());

        assertThat(aPackage).isFalse();
    }

    @Test
    public void should_isPackage_return_false_for_class_in_compilation_unit() throws ClassNotFoundException {
        boolean aPackage = classLoaderEnvironment.isPackage(charsArray("a.b.c"), "MyClass1".toCharArray());

        assertThat(aPackage).isFalse();
    }

    @Test
    public void should_find_type_return_type_from_classloader() throws ClassNotFoundException {
        doReturn(Thread.currentThread().getContextClassLoader().getResourceAsStream("java/lang/String.class")).when(classloader).getResourceAsStream("java/lang/String.class");

        NameEnvironmentAnswer type = classLoaderEnvironment.findType(charsArray("java.lang.String"));
        assertThat(type).isNotNull();

        //redo to use cache
        type = classLoaderEnvironment.findType(charsArray("java.lang.String"));
        assertThat(type).isNotNull();
    }
    @Test
    public void should_find_type_return_type2_from_classloader() throws ClassNotFoundException {
        doReturn(Thread.currentThread().getContextClassLoader().getResourceAsStream("java/lang/String.class")).when(classloader).getResourceAsStream("java/lang/String.class");

        NameEnvironmentAnswer type = classLoaderEnvironment.findType("String".toCharArray(), charsArray("java.lang"));
        assertThat(type).isNotNull();

        //redo to use cache
        type = classLoaderEnvironment.findType(charsArray("java.lang.String"));
        assertThat(type).isNotNull();
        classLoaderEnvironment.cleanup();
    }


    @Test
    public void should_find_type_return_null_when_bad_class() throws ClassNotFoundException {
        doReturn(new ByteArrayInputStream("/** plop*/".getBytes())).when(classloader).getResourceAsStream("java/lang/String.class");

        NameEnvironmentAnswer type = classLoaderEnvironment.findType("String".toCharArray(), charsArray("java.lang"));

        assertThat(type).isNull();
    }
    @Test
    public void should_find_type_return_null_when_not_found() throws ClassNotFoundException {

        NameEnvironmentAnswer type = classLoaderEnvironment.findType(charsArray("java.lang.String"));

        assertThat(type).isNull();
    }


}