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

import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import jakarta.servlet.http.MappingMatch;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.bonitasoft.web.rest.server.api.RestControllerUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Guards the path matching of the MockMvc setup shared by all the controller tests
 * ({@code RestControllerUtils}), which mirrors the production one configured in
 * {@code SpringWebConfiguration#configurePathMatch}.
 * <p>
 * That mirror is invisible to the controller tests themselves: their mock requests have an empty
 * servlet path and no servlet mapping, so parsed patterns and {@code AntPathMatcher} resolve an
 * absolute {@code @RequestMapping} identically. Should the mirror ever stop selecting the same mode as
 * production - the legacy opt-in Spring 7 removes - every controller test would still pass while
 * production returned 404. Stamping the request with the servlet mapping Tomcat would report makes the
 * difference observable.
 * <p>
 * <b>The mirror covers the matching mode only, not full-path matching:</b>
 * {@code StandaloneMockMvcBuilder} exposes no way to set {@code alwaysUseFullPath}, so the lookup path
 * is the path within the servlet mapping. The base-path request below still resolves because
 * {@code UrlPathHelper#getPathWithinServletMapping} finds an empty remainder and falls back to the path
 * within the application. Do not extend this test with a sub-path request expecting a 200: with the
 * same stamped mapping, {@code /API/test/mirror/42} resolves to a lookup path of {@code /42} and 404s
 * here, while production - which does use the full path - serves it. That gap is harmless because no
 * controller test stamps a servlet mapping, and it is what {@code SpringWebConfigurationTest} covers
 * against the production configuration.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ControllerTestPathMatchingTest {

    private static final String RESOURCE_PATH = "/API/test/mirror";

    /**
     * /!\ This fixture must stay outside the {@code *.web.rest.server.api} packages: they are
     * component-scanned by {@code SpringWebConfiguration} and validated by
     * {@code ServletMappingValidationTest}, which sees the test classpath too and would demand a
     * web.xml url-pattern for this path.
     */
    @RestController
    @RequestMapping(RESOURCE_PATH)
    static class TestController extends AbstractRESTController {

        @GetMapping
        String search() {
            return "search";
        }
    }

    @Mock
    private APISession apiSession;

    @Test
    void initMockMvcWithSessionAttributes_should_use_the_legacy_path_matcher_like_production() throws Exception {
        //given
        var sessionAttributes = new HashMap<String, Object>();
        MockMvc mockMvc = RestControllerUtils.initMockMvcWithSessionAttributes(spy(new TestController()),
                sessionAttributes, apiSession);

        //when-then
        mockMvc.perform(get(RESOURCE_PATH)
                .sessionAttrs(sessionAttributes)
                .servletPath(RESOURCE_PATH)
                // as Tomcat reports it for the /API/test/mirror/* servlet mapping
                .with(request -> {
                    request.setHttpServletMapping(new MockHttpServletMapping("", RESOURCE_PATH + "/*",
                            "SpringRest", MappingMatch.PATH));
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("search"));
    }
}
