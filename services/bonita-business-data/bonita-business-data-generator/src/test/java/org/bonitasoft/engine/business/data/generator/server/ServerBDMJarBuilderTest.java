/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.generator.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.generator.BOMBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerBDMJarBuilderTest {

    @Mock
    private BusinessObjectModel bom;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File directory;
    private ServerBDMJarBuilder bdmJarBuilder;

    @Before
    public void setUp() throws Exception {
        directory = temporaryFolder.newFolder();
        bdmJarBuilder = new ServerBDMJarBuilder(new ServerBDMCodeGenerator());
    }

    @Test
    public void should_addPersistenceUnittestGetPersistenceFileContentFor() throws Exception {
        bdmJarBuilder.addPersistenceFile(directory, bom);

        verify(bom).getBusinessObjects();
        assertThat(directory).isDirectory();
        final File metaInf = new File(directory, "META-INF");
        assertThat(metaInf).exists();
        assertThat(new File(metaInf, "persistence.xml")).exists();
    }

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_go_well_without_errors() throws Exception {
        bdmJarBuilder.build(BOMBuilder.aBOM().build(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildComplex(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries2() throws Exception {
        bdmJarBuilder.build(BOMBuilder.aBOM().buildPerson(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_with_multipleBoolean() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildModelWithMultipleBoolean(), TrueFileFilter.TRUE);
    }

    /**
     * BPA-833: the alias must give way to an <em>inherited</em> method too. A BOOLEAN attribute named
     * <code>Class</code> would produce <code>getClass()</code>, which cannot override the final
     * {@link Object#getClass()} - the entity fails to compile rather than merely carrying a duplicate.
     */
    @Test
    public void jar_builder_should_go_well_with_a_boolean_named_after_an_inherited_getter() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        // the assertion is that validation, generation and compilation all succeed
        assertThatNoException()
                .isThrownBy(() -> bdmJarBuilder.build(builder.buildModelWithBooleanNamedClass(), TrueFileFilter.TRUE));
    }

    /**
     * BPA-833: a BOOLEAN attribute whose name matches a LAZY relation's case-insensitively. The two accessors are
     * distinct methods, so this compiles either way; the alias is skipped because the client lazy-loading proxy
     * keys on the lower-cased name and would otherwise treat the relation as already loaded.
     */
    @Test
    public void jar_builder_should_go_well_with_a_boolean_colliding_in_case_with_a_lazy_relation() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        // the assertion is that validation, generation and compilation all succeed
        assertThatNoException()
                .isThrownBy(() -> bdmJarBuilder.build(
                        builder.buildModelWithBooleanCollidingCaseInsensitivelyWithLazyRelation(),
                        TrueFileFilter.TRUE));
    }

    /**
     * BPA-833: a BOOLEAN attribute colliding with a relation on the alias name. Driving the full chain (validate,
     * generate, compile) proves both that such a model is accepted by {@code BusinessObjectModelValidator} (the
     * premise for needing the guard at all) and that the guard keeps the generated source compiling. A duplicate
     * method surfaces as a compiler error during BDM install, which an assertion on the code model would miss.
     */
    @Test
    public void jar_builder_should_go_well_with_a_boolean_colliding_with_a_relation() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        // the assertion is that validation, generation and compilation all succeed
        assertThatNoException()
                .isThrownBy(() -> bdmJarBuilder.build(builder.buildModelWithBooleanCollidingWithRelation(),
                        TrueFileFilter.TRUE));
    }

    /**
     * BPA-833: a single BOOLEAN attribute is generated with both <code>isHungry()</code> and its
     * <code>getHungry()</code> alias - the two accessors must still compile together.
     */
    @Test
    public void jar_builder_should_go_well_with_singleBoolean() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        // the assertion is that validation, generation and compilation all succeed
        assertThatNoException()
                .isThrownBy(() -> bdmJarBuilder.build(builder.buildModelWithSingleBoolean(), TrueFileFilter.TRUE));
    }

}
