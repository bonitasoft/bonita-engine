/**
 * Copyright (C) 2026 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.business.data.generator.server.ServerBDMCodeGenerator;
import org.bonitasoft.engine.business.data.generator.server.ServerBDMJarBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * BPA-833: guards that a BOOLEAN attribute stays readable through the <code>entity.attribute</code> shorthand that
 * process expressions (gateway conditions, connector inputs, operations...) are written with.
 * <p>
 * Groovy 4 treats <code>isX()</code> as a property getter only when it returns a primitive <code>boolean</code>. A
 * BOOLEAN attribute is generated with the wrapper, so the shorthand falls back to reading the private field, which
 * Groovy 4 refuses to do across a class hierarchy. Without the generated <code>getX()</code> alias the proxy case
 * therefore fails with <code>MissingPropertyException</code>, while the concrete entity, which declares the field
 * itself, keeps working.
 * <p>
 * Driven through the real server generation chain (generate, compile, load), which runs once for the whole class
 * since both tests read the same entity.
 */
public class BooleanPropertyShorthandTest {

    private static final String DINOSAUR_QUALIFIED_NAME = "com.company.Dinosaur";

    private static final String GATEWAY_CONDITION = "return dinosaur.hungry";

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static URLClassLoader bdmClassLoader;

    private static Class<?> dinosaurClass;

    private static Class<?> lazyProxyClass;

    @BeforeClass
    public static void generateAndLoadBdm() throws Exception {
        final byte[] bdmJar = new ServerBDMJarBuilder(new ServerBDMCodeGenerator())
                .build(new BOMBuilder().buildModelWithSingleBoolean(), TrueFileFilter.TRUE);
        final File jarFile = temporaryFolder.newFile("server-bdm.jar");
        Files.write(jarFile.toPath(), bdmJar);
        bdmClassLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() },
                BooleanPropertyShorthandTest.class.getClassLoader());
        dinosaurClass = bdmClassLoader.loadClass(DINOSAUR_QUALIFIED_NAME);
        lazyProxyClass = lazyProxyOf(dinosaurClass, bdmClassLoader);
    }

    @AfterClass
    public static void closeBdmClassLoader() throws Exception {
        if (bdmClassLoader != null) {
            bdmClassLoader.close();
        }
    }

    @Test
    public void generatedBooleanAttribute_should_be_readable_as_a_groovy_property_on_the_concrete_entity()
            throws Exception {
        //given
        final Object dinosaur = hungryDinosaur(dinosaurClass);

        //when
        final Object result = evaluateGatewayCondition(dinosaur);

        //then
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void generatedBooleanAttribute_should_be_readable_as_a_groovy_property_on_a_lazy_proxy()
            throws Exception {
        //given
        final Object dinosaur = hungryDinosaur(lazyProxyClass);

        //when
        final Object result = evaluateGatewayCondition(dinosaur);

        //then
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    /**
     * Stands in for the proxy an expression actually receives, reproducing the one condition BPA-833 turns on: a
     * generated subclass, so the private field backing the attribute lives on the superclass. The
     * <code>_$$_jvst</code> naming matches the reported stack trace, whose proxy comes from
     * <code>ServerProxyfier</code>; Hibernate's own lazy-loading proxy is a further layer and just as sufficient.
     */
    private static Class<?> lazyProxyOf(final Class<?> entityClass, final ClassLoader classLoader) throws Exception {
        final ClassPool classPool = new ClassPool(true);
        classPool.appendClassPath(new LoaderClassPath(classLoader));
        return classPool.makeClass(entityClass.getName() + "_$$_jvstd98_0", classPool.get(entityClass.getName()))
                .toClass(entityClass);
    }

    private Object hungryDinosaur(final Class<?> instantiatedClass) throws Exception {
        final Object dinosaur = instantiatedClass.getConstructor().newInstance();
        dinosaurClass.getMethod("setHungry", Boolean.class).invoke(dinosaur, Boolean.TRUE);
        return dinosaur;
    }

    private Object evaluateGatewayCondition(final Object dinosaur) {
        final Binding binding = new Binding();
        binding.setVariable("dinosaur", dinosaur);
        return new GroovyShell(bdmClassLoader, binding).evaluate(GATEWAY_CONDITION);
    }

}
