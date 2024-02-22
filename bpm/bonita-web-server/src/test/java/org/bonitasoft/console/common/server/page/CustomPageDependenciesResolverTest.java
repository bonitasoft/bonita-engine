/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageDependenciesResolverTest {

    @Mock
    private WebBonitaConstantsUtils webBonitaConstantsUtils;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void should_throw_an_IllegalStateException_when_accessing_tmp_folder_before_resolving_libraries()
            throws Exception {
        final CustomPageDependenciesResolver resolver = newCustomPageDependenciesResolver(null);

        expectedException.expect(IllegalStateException.class);

        resolver.getTempFolder();
    }

    @Test
    public void should_resolve_dependencies_content() throws Exception {
        final CustomPageDependenciesResolver resolver = newCustomPageDependenciesResolver(testPageFolder());

        final Map<String, byte[]> dependenciesContent = resolver.resolveCustomPageDependencies();

        assertThat(dependenciesContent).containsKeys("resource.properties",
                "bdm-client.jar",
                "bdm-dao.jar",
                "javassist-3.18.1-GA.jar",
                "util.jar");
        assertThat(CustomPageDependenciesResolver.PAGES_LIB_TMPDIR).containsKey("myCustomPage");
        final File cachedTmpFoler = CustomPageDependenciesResolver.PAGES_LIB_TMPDIR.get("myCustomPage");
        assertThat(cachedTmpFoler.exists()).isTrue();
        assertThat(resolver.getTempFolder()).isEqualTo(cachedTmpFoler);
    }

    @Test
    public void should_delete_temporary_lib_foler() throws Exception {
        final CustomPageDependenciesResolver resolver = newCustomPageDependenciesResolver(testPageFolder());
        resolver.resolveCustomPageDependencies();

        assertThat(CustomPageDependenciesResolver.PAGES_LIB_TMPDIR).containsKey("myCustomPage");
        assertThat(CustomPageDependenciesResolver.PAGES_LIB_TMPDIR.get("myCustomPage").exists()).isTrue();

        final File tempFolder = CustomPageDependenciesResolver.removePageLibTempFolder("myCustomPage");

        assertThat(CustomPageDependenciesResolver.PAGES_LIB_TMPDIR).doesNotContainKey("myCustomPage");
        assertThat(tempFolder.exists()).isFalse();
    }

    @Test
    public void should_resolve_dependencies_return_an_empty_map_if_no_lib_folder_is_found_in_custom_page()
            throws Exception {
        final CustomPageDependenciesResolver resolver = newCustomPageDependenciesResolver(null);

        final Map<String, byte[]> dependenciesContent = resolver.resolveCustomPageDependencies();

        assertThat(dependenciesContent).isEmpty();
    }

    private File testPageFolder() {
        return new File(CustomPageDependenciesResolverTest.class.getResource("/ARootPageFolder").getFile());
    }

    private CustomPageDependenciesResolver newCustomPageDependenciesResolver(File folder) throws IOException {
        when(webBonitaConstantsUtils.getTempFolder()).thenReturn(tmpFolder.newFolder());
        return new CustomPageDependenciesResolver("myCustomPage", folder, webBonitaConstantsUtils);
    }

}
