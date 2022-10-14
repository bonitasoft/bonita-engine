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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageChildFirstClassLoaderTest {

    @Mock
    private CustomPageDependenciesResolver customPageDependenciesResolver;
    @Mock
    private BDMClientDependenciesResolver bdmDependenciesResolver;

    private CustomPageChildFirstClassLoader classLoader;

    @Rule
    public TemporaryFolder tmpRule = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        when(customPageDependenciesResolver.getTempFolder()).thenReturn(tmpRule.newFolder());
    }

    @After
    public void tearDown() throws Exception {
        if (classLoader != null) {
            classLoader.close();
        }
    }

    @Test
    public void should_add_custom_page_jar_resources_in_classloader_urls() throws Exception {
        classLoader = newClassloader();
        when(customPageDependenciesResolver.resolveCustomPageDependencies()).thenReturn(loadedResources("util.jar"));

        classLoader.addCustomPageResources();

        assertThat(classLoader.getURLs()).hasSize(1);
    }

    @Test
    public void should_not_add_duplicated_bdm_dependencies_in_lib_folder_in_classloader() throws Exception {
        classLoader = newClassloader();
        when(customPageDependenciesResolver.resolveCustomPageDependencies())
                .thenReturn(loadedResources("util.jar", "bdm-model.jar", "bdm-dao.jar", "javassist-3.18.1-GA.jar"));
        when(bdmDependenciesResolver.isABDMDependency("bdm-model.jar")).thenReturn(true);
        when(bdmDependenciesResolver.isABDMDependency("bdm-dao.jar")).thenReturn(true);
        when(bdmDependenciesResolver.isABDMDependency("javassist-3.18.1-GA.jar")).thenReturn(true);

        classLoader.addCustomPageResources();

        assertThat(classLoader.getURLs()).hasSize(1);
    }

    @Test
    public void should__add_bdm_dependencies_in_classloader_before_other_dependencies() throws Exception {
        classLoader = spy(newClassloader());
        final URL[] bdmDependenciesURLs = bdmDependenciesURLs();
        when(bdmDependenciesResolver.getBDMDependencies()).thenReturn(bdmDependenciesURLs);
        when(customPageDependenciesResolver.resolveCustomPageDependencies())
                .thenReturn(loadedResources("util.jar"));
        when(bdmDependenciesResolver.isABDMDependency("bdm-model.jar")).thenReturn(true);
        when(bdmDependenciesResolver.isABDMDependency("bdm-dao.jar")).thenReturn(true);
        when(bdmDependenciesResolver.isABDMDependency("javassist-3.18.1-GA.jar")).thenReturn(true);
        classLoader.addCustomPageResources();

        final InOrder order = inOrder(classLoader, customPageDependenciesResolver);
        order.verify(classLoader).addURLs(bdmDependenciesURLs);
        order.verify(customPageDependenciesResolver).resolveCustomPageDependencies();
        assertThat(classLoader.getURLs()).hasSize(4);
    }

    private URL[] bdmDependenciesURLs() throws MalformedURLException {
        final URL rootFolderURL = CustomPageChildFirstClassLoader.class.getResource("/bdmDependencies");
        final File rootFolder = new File(rootFolderURL.getFile());
        final List<URL> urls = new ArrayList<>();
        for (final File dep : rootFolder.listFiles()) {
            urls.add(dep.toURI().toURL());
        }
        return urls.toArray(new URL[urls.size()]);
    }

    @Test
    public void should_add_custom_page_non_jar_resources_in_classloader() throws Exception {
        classLoader = newClassloader();
        when(customPageDependenciesResolver.resolveCustomPageDependencies())
                .thenReturn(loadedResources("util.properties"));

        classLoader.addCustomPageResources();

        assertThat(classLoader.getURLs()).isEmpty();
        assertThat(classLoader.getResourceAsStream("util.properties")).isNotNull();
    }

    @Test
    public void should_get_resources_contained_in_jars_of_classloader() throws Exception {
        classLoader = spy(newClassloader());
        when(customPageDependenciesResolver.resolveCustomPageDependencies()).thenReturn(loadedResources("util.jar"));
        classLoader.addCustomPageResources();

        InputStream resourceNotExistingButShouldBeSoughtInJars = classLoader.getResourceAsStream("util.properties");

        assertThat(resourceNotExistingButShouldBeSoughtInJars).isNull();
        verify(classLoader).getResourceAsStreamRegular("util.properties");

    }

    private Map<String, byte[]> loadedResources(String... resourceNames) {
        final Map<String, byte[]> resources = new HashMap<>();
        for (final String resource : resourceNames) {
            resources.put(resource, new byte[0]);
        }
        return resources;
    }

    private CustomPageChildFirstClassLoader newClassloader() {
        return new CustomPageChildFirstClassLoader("myPage", customPageDependenciesResolver, bdmDependenciesResolver,
                Thread.currentThread().getContextClassLoader());
    }

}
