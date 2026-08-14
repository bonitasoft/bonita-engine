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
package org.bonitasoft.web.rest.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import jakarta.servlet.http.MappingMatch;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

/**
 * Guards the path-matching configuration of the REST API handler mappings.
 * <p>
 * Our controllers declare absolute patterns ({@code /API/...}, {@code /APISpringInternal/...}) while
 * the {@code SpringRest} DispatcherServlet is partly mapped with path-prefix patterns in web.xml.
 * This only works when the handler mappings match on the full request path, which requires the
 * legacy {@code AntPathMatcher} mode: with the parsed-pattern mode (the Spring 6 default), the
 * servlet path is stripped from the lookup path of every prefix-mapped request
 * ({@code MappingMatch.PATH}) and no controller pattern can match.
 */
class SpringWebConfigurationTest {

    private static final String RESOURCE_PATH = "/API/test/resource";

    private static final String CATCH_ALL_RESOURCE_PATH = "/API/test/ext";
    private static final String TEMPLATED_RESOURCE_PATH = "/API/test/bdm";

    private static final String INTERNAL_SERVLET_PATH = "/APISpringInternal";
    private static final String INTERNAL_RESOURCE_PATH = INTERNAL_SERVLET_PATH + "/test/{id}/resource";

    /**
     * /!\ These fixtures must stay outside the {@code *.web.rest.server.api} packages: they are
     * component-scanned by {@code SpringWebConfiguration} and validated by
     * {@code ServletMappingValidationTest}, which sees the test classpath too and would demand a
     * web.xml url-pattern for their paths.
     */
    @RestController
    @RequestMapping(RESOURCE_PATH)
    static class TestController {

        @GetMapping
        String search() {
            return "search";
        }

        @PutMapping("/{id}")
        String update() {
            return "update";
        }
    }

    /** Shape of the resources reached through the {@code /APISpringInternal/*} rewrite. */
    @RestController
    @RequestMapping(INTERNAL_RESOURCE_PATH)
    static class TestInternalController {

        @GetMapping
        String internalGet() {
            return "internalGet";
        }
    }

    /**
     * Shape of {@code ApiExtensionController}: a catch-all sub-path. This is the one pattern whose
     * semantics differ between the two matchers, and the widest surface of the REST API.
     */
    @RestController
    @RequestMapping(CATCH_ALL_RESOURCE_PATH)
    static class TestCatchAllController {

        @RequestMapping("/**")
        String catchAll() {
            return "catchAll";
        }
    }

    /**
     * Shape of {@code BusinessDataController} / {@code BdmCRUDController}: a templated base path with an
     * endpoint on the base path itself, the variable holding a dotted class name. Such a resource can
     * only ever be served by a path-prefix mapping, so no exact url-pattern can protect it.
     */
    @RestController
    @RequestMapping(TEMPLATED_RESOURCE_PATH + "/{className}")
    static class TestTemplatedController {

        @GetMapping("")
        String templatedGet() {
            return "templatedGet";
        }
    }

    @Test
    void requestMappingHandlerMapping_should_match_on_full_path_instead_of_parsed_patterns() {
        //given-when
        RequestMappingHandlerMapping handlerMapping = buildProductionHandlerMapping();

        //then
        assertThat(handlerMapping.usesPathPatterns())
                .withFailMessage("""
                        The handler mapping uses parsed path patterns: the servlet path of every \
                        prefix-mapped request (/API/some/resource/*, /APISpringInternal/*) would be \
                        stripped from the lookup path and the controllers, which declare absolute \
                        patterns, would all return 404.
                        Full-path matching must be configured through PathMatchConfigurer \
                        (setAlwaysUseFullPath on the handler mapping bean does NOT disable the \
                        parsed patterns).""")
                .isFalse();
        assertThat(handlerMapping.getUrlPathHelper()
                .getLookupPathForRequest(prefixMappedRequest("GET", RESOURCE_PATH, RESOURCE_PATH)))
                .isEqualTo(RESOURCE_PATH);
    }

    /**
     * End-to-end guard: a request routed by a path-prefix servlet mapping
     * ({@code MappingMatch.PATH}, as {@code /API/test/resource/*} would be in web.xml) must still
     * reach the handler of a controller declaring the absolute path. This fails with the
     * parsed-pattern default, on the base path as well as on a sub-path.
     */
    @Test
    void requestMappingHandlerMapping_should_find_handler_for_request_routed_by_a_prefix_servlet_mapping()
            throws Exception {
        //given
        RequestMappingHandlerMapping handlerMapping = buildProductionHandlerMapping();

        //when
        var basePathHandler = handlerMapping.getHandler(
                prefixMappedRequest("GET", RESOURCE_PATH, RESOURCE_PATH));
        var subPathHandler = handlerMapping.getHandler(
                prefixMappedRequest("PUT", RESOURCE_PATH + "/42", RESOURCE_PATH));

        //then
        assertThat(handlerMethodNameOf(basePathHandler))
                .withFailMessage("GET %s routed by the %s/* servlet mapping did not reach the search handler",
                        RESOURCE_PATH, RESOURCE_PATH)
                .isEqualTo("search");
        assertThat(handlerMethodNameOf(subPathHandler))
                .withFailMessage("PUT %s/42 routed by the %s/* servlet mapping did not reach the update handler",
                        RESOURCE_PATH, RESOURCE_PATH)
                .isEqualTo("update");
    }

    /**
     * Same guard for the shape of the resources served through the {@code /APISpringInternal/*}
     * mapping: a short servlet path, the whole resource path in the path info, and a path variable in
     * the middle. This is the shape most of the REST API depends on (process design, process contract,
     * process instantiation, user task execution and context...).
     */
    @Test
    void requestMappingHandlerMapping_should_find_handler_for_request_routed_by_the_internal_servlet_mapping()
            throws Exception {
        //given
        RequestMappingHandlerMapping handlerMapping = buildProductionHandlerMapping();

        //when
        var handler = handlerMapping.getHandler(
                prefixMappedRequest("GET", INTERNAL_SERVLET_PATH + "/test/42/resource", INTERNAL_SERVLET_PATH));

        //then
        assertThat(handlerMethodNameOf(handler))
                .withFailMessage("GET %s/test/42/resource routed by the %s/* servlet mapping did not reach the handler",
                        INTERNAL_SERVLET_PATH, INTERNAL_SERVLET_PATH)
                .isEqualTo("internalGet");
    }

    /**
     * The majority of the resources are routed by an exact servlet mapping ({@code MappingMatch.EXACT},
     * e.g. {@code /API/form/mapping}) - the mapping style this fix adds nine more of. Those were never
     * broken, so this pins that the switch to full-path matching leaves them resolving too.
     */
    @Test
    void requestMappingHandlerMapping_should_find_handler_for_request_routed_by_an_exact_servlet_mapping()
            throws Exception {
        //given
        RequestMappingHandlerMapping handlerMapping = buildProductionHandlerMapping();

        //when
        var handler = handlerMapping.getHandler(exactMappedRequest("GET", RESOURCE_PATH));

        //then
        assertThat(handlerMethodNameOf(handler))
                .withFailMessage("GET %s routed by the exact %s servlet mapping did not reach the search handler",
                        RESOURCE_PATH, RESOURCE_PATH)
                .isEqualTo("search");
    }

    /**
     * QA smoke item 1 as a unit test: {@code /**} is the only pattern whose semantics genuinely differ
     * between {@code AntPathMatcher} (which matches the parent path too) and {@code PathPatternParser},
     * and {@code ApiExtensionController} - mapped exactly like this - is the widest surface of the API.
     */
    @Test
    void requestMappingHandlerMapping_should_find_handler_for_a_catch_all_sub_path() throws Exception {
        //given
        RequestMappingHandlerMapping handlerMapping = buildProductionHandlerMapping();

        //when
        var handler = handlerMapping.getHandler(
                prefixMappedRequest("GET", CATCH_ALL_RESOURCE_PATH + "/a/b", CATCH_ALL_RESOURCE_PATH));

        //then
        assertThat(handlerMethodNameOf(handler))
                .withFailMessage("GET %s/a/b did not reach the /** handler", CATCH_ALL_RESOURCE_PATH)
                .isEqualTo("catchAll");
    }

    /**
     * QA smoke item 2 as a unit test: a templated base path with an endpoint on the base path, the
     * variable holding a dotted class name ({@code BusinessDataController}, {@code BdmCRUDController}).
     * Such a resource is only ever routed by a path-prefix mapping, so the exact-pattern rule of
     * {@code ServletMappingValidationTest} structurally cannot protect it - this does.
     */
    @Test
    void requestMappingHandlerMapping_should_find_handler_for_a_templated_base_path_with_a_dotted_variable()
            throws Exception {
        //given
        RequestMappingHandlerMapping handlerMapping = buildProductionHandlerMapping();
        String className = "com.company.model.Person";
        var request = prefixMappedRequest("GET", TEMPLATED_RESOURCE_PATH + "/" + className,
                TEMPLATED_RESOURCE_PATH);

        //when
        var handler = handlerMapping.getHandler(request);

        //then
        assertThat(handlerMethodNameOf(handler))
                .withFailMessage("GET %s/%s did not reach the templated handler", TEMPLATED_RESOURCE_PATH,
                        className)
                .isEqualTo("templatedGet");
        assertThat(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .describedAs("the dotted segment must be captured whole, suffix pattern matching excluded")
                .isEqualTo(Map.of("className", className));
    }

    private String handlerMethodNameOf(HandlerExecutionChain handlerExecutionChain) {
        if (handlerExecutionChain == null) {
            return null;
        }
        return ((HandlerMethod) handlerExecutionChain.getHandler()).getMethod().getName();
    }

    /**
     * The handler mapping as the production {@code SpringWebConfiguration} builds it, with the test
     * controllers as the only handlers.
     */
    private RequestMappingHandlerMapping buildProductionHandlerMapping() {
        StaticWebApplicationContext webApplicationContext = new StaticWebApplicationContext();
        webApplicationContext.setServletContext(new MockServletContext());
        webApplicationContext.registerSingleton("testController", TestController.class);
        webApplicationContext.registerSingleton("testInternalController", TestInternalController.class);
        webApplicationContext.registerSingleton("testCatchAllController", TestCatchAllController.class);
        webApplicationContext.registerSingleton("testTemplatedController", TestTemplatedController.class);
        webApplicationContext.refresh();

        SpringWebConfiguration configuration = new SpringWebConfiguration();
        configuration.setApplicationContext(webApplicationContext);
        configuration.setServletContext(webApplicationContext.getServletContext());

        RequestMappingHandlerMapping handlerMapping = configuration.requestMappingHandlerMapping(
                configuration.mvcContentNegotiationManager(),
                configuration.mvcConversionService(),
                configuration.mvcResourceUrlProvider());
        handlerMapping.setApplicationContext(webApplicationContext);
        handlerMapping.afterPropertiesSet();
        return handlerMapping;
    }

    /**
     * A request as Tomcat exposes it when the DispatcherServlet is mapped on a path prefix: the
     * servlet path is the mapping prefix, and the mapping match is {@code PATH}.
     */
    private MockHttpServletRequest prefixMappedRequest(String method, String path, String servletPath) {
        MockHttpServletRequest request = mappedRequest(method, path, servletPath);
        request.setPathInfo(path.equals(servletPath) ? null : path.substring(servletPath.length()));
        request.setHttpServletMapping(
                new MockHttpServletMapping("", servletPath + "/*", "SpringRest", MappingMatch.PATH));
        // done by the DispatcherServlet before handler resolution when parsed patterns are in use
        ServletRequestPathUtils.parseAndCache(request);
        return request;
    }

    /**
     * A request as Tomcat exposes it when the DispatcherServlet is mapped on that exact path: the
     * servlet path is the whole path, there is no path info, and the mapping match is {@code EXACT}.
     */
    private MockHttpServletRequest exactMappedRequest(String method, String path) {
        MockHttpServletRequest request = mappedRequest(method, path, path);
        request.setHttpServletMapping(new MockHttpServletMapping("", path, "SpringRest", MappingMatch.EXACT));
        ServletRequestPathUtils.parseAndCache(request);
        return request;
    }

    private MockHttpServletRequest mappedRequest(String method, String path, String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/bonita" + path);
        request.setContextPath("/bonita");
        request.setServletPath(servletPath);
        return request;
    }
}
