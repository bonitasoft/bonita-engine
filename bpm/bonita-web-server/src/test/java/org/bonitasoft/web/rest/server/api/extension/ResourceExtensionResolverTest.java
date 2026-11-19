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
package org.bonitasoft.web.rest.server.api.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.console.common.server.page.PageMappingService;
import org.bonitasoft.console.common.server.page.PageReference;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceExtensionResolverTest {

    public static final String API_EXTENSION_POST_MAPPING_KEY = "apiExtension|POST|myPostResource";
    public static final String API_EXTENSION_GET_MAPPING_KEY = "apiExtension|GET|helloWorld";

    public static final long PAGE_ID = 2L;

    @Mock
    private PageResourceProviderImpl pageResourceProvider;

    @Mock
    private APISession apiSession;

    @Mock
    private PageMappingService pageMappingService;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    private PageReference pageReference;

    @BeforeEach
    void before() throws Exception {
        final URL resource = ResourceExtensionResolverTest.class.getResource("page.properties");
        doReturn(new FileInputStream(new File(resource.toURI()))).when(pageResourceProvider)
                .getResourceAsStream("page.properties");
    }

    @Test
    void should_post_resolve_class_file_name() throws Exception {
        //given
        var resourceExtensionResolver = createResolver("POST", "/bonita/API/extension/myPostResource");

        //when
        final ControllerClassName controllerClassName = resourceExtensionResolver
                .resolveRestApiControllerClassName(pageResourceProvider);

        //then
        assertThat(controllerClassName.isSource()).isTrue();
        assertThat(controllerClassName.getName()).isEqualTo("PostResource.groovy");
    }

    @Test
    void should_get_resolve_class_file_name() throws Exception {
        //given
        var resourceExtensionResolver = createResolver("GET", "/bonita/API/extension/helloWorld");

        //when
        final ControllerClassName restApiControllerClassName = resourceExtensionResolver
                .resolveRestApiControllerClassName(pageResourceProvider);

        //then
        assertThat(restApiControllerClassName.isSource()).isTrue();
        assertThat(restApiControllerClassName.getName()).isEqualTo("Index.groovy");
    }

    @Test
    void should_get_resolve_class_name() throws Exception {
        //given
        var resourceExtensionResolver = createResolver("GET", "/bonita/API/extension/myCompiledRestApi");

        //when
        final ControllerClassName restApiControllerClassName = resourceExtensionResolver
                .resolveRestApiControllerClassName(pageResourceProvider);

        //then
        assertThat(restApiControllerClassName.isSource()).isFalse();
        assertThat(restApiControllerClassName.getName()).isEqualTo("com.company.MyController");
    }

    @Test
    void should_get_resolve_class_name_over_class_file_name_when_both_are_present() throws Exception {
        //given
        var resourceExtensionResolver = createResolver("GET", "/bonita/API/extension/myCompiledRestApi2");

        //when
        final ControllerClassName restApiControllerClassName = resourceExtensionResolver
                .resolveRestApiControllerClassName(pageResourceProvider);

        //then
        assertThat(restApiControllerClassName.isSource()).isFalse();
        assertThat(restApiControllerClassName.getName()).isEqualTo("com.company.MyController");
    }

    @Test
    void should_resolve_class_file_name_when_name_begins_the_same_than_another() throws Exception {
        // two api extensions with names that begins the same are installed (myPostResource and myPostResourceB)
        // see https://bonitasoft.atlassian.net/browse/BS-16791
        var resourceExtensionResolver = createResolver("POST", "/bonita/API/extension/myPostResourceB");

        final ControllerClassName controllerClassName = resourceExtensionResolver
                .resolveRestApiControllerClassName(pageResourceProvider);

        assertThat(controllerClassName.isSource()).isTrue();
        assertThat(controllerClassName.getName()).isEqualTo("PostResourceB.groovy");
    }

    @Test
    void should_not_resolve_class_file_name() {
        //given
        var resourceExtensionResolver = createResolver("POST", "/bonita/API/extension/notResource");

        //when then exception
        assertThatThrownBy(() -> resourceExtensionResolver.resolveRestApiControllerClassName(pageResourceProvider))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("error while getting resource:apiExtension|POST|notResource");
    }

    @Test
    void should_generate_mapping_key() {
        //given
        var resourceExtensionResolver = createResolver("POST", "/bonita/API/extension/myPostResource");

        //when
        final String mappingKey = resourceExtensionResolver.generateMappingKey();

        //then
        assertThat(mappingKey).isEqualTo(API_EXTENSION_POST_MAPPING_KEY);
    }

    @Test
    void should_resolve_pageId() throws Exception {
        //given
        doReturn(pageReference).when(pageMappingService).getPage(httpServletRequest, apiSession,
                API_EXTENSION_POST_MAPPING_KEY, Locale.FRENCH, false);
        doReturn(PAGE_ID).when(pageReference).getPageId();

        var resourceExtensionResolver = createResolver("POST", "/bonita/API/extension/myPostResource");

        //when
        final Long pageId = resourceExtensionResolver.resolvePageId(apiSession);

        //then
        verify(pageMappingService).getPage(any(HttpServletRequest.class), eq(apiSession),
                eq(API_EXTENSION_POST_MAPPING_KEY),
                any(Locale.class), eq(false));
        assertThat(pageId).isEqualTo(PAGE_ID);
    }

    @Test
    void should_resolve_pageId_with_parameters() throws Exception {
        //given
        doReturn(pageReference).when(pageMappingService).getPage(httpServletRequest, apiSession,
                API_EXTENSION_GET_MAPPING_KEY, Locale.FRENCH, false);
        doReturn(PAGE_ID).when(pageReference).getPageId();

        var resourceExtensionResolver = createResolver("GET", "/bonita/API/extension/helloWorld");

        //when
        final Long pageId = resourceExtensionResolver.resolvePageId(apiSession);

        //then
        verify(pageMappingService).getPage(any(HttpServletRequest.class), eq(apiSession),
                eq(API_EXTENSION_GET_MAPPING_KEY),
                any(Locale.class), eq(false));
        assertThat(pageId).isEqualTo(PAGE_ID);
    }

    @Test
    void should_mapping_key_exclude_parameters() {
        //given
        var resourceExtensionResolver = createResolver("GET", "/bonita/API/extension/helloWorld");

        //when
        final String mappingKey = resourceExtensionResolver.generateMappingKey();

        //then
        assertThat(mappingKey).isEqualTo(API_EXTENSION_GET_MAPPING_KEY);
    }

    @Test
    void should_unresolved_pageId_throw_exception() throws Exception {
        //given
        final NotFoundException notFoundException = new NotFoundException("page not found");
        doThrow(notFoundException).when(pageMappingService).getPage(httpServletRequest, apiSession,
                API_EXTENSION_POST_MAPPING_KEY, Locale.FRENCH, false);

        var resourceExtensionResolver = createResolver("POST", "/bonita/API/extension/myPostResource");

        //when then exception
        assertThatThrownBy(() -> resourceExtensionResolver.resolvePageId(apiSession))
                .isInstanceOf(NotFoundException.class);
    }

    private ResourceExtensionResolver createResolver(String httpMethod, String uri) {
        doReturn(uri).when(httpServletRequest).getRequestURI();
        doReturn(Locale.FRENCH).when(httpServletRequest).getLocale();

        return new ResourceExtensionResolver(httpServletRequest, httpMethod, pageMappingService);
    }

}
