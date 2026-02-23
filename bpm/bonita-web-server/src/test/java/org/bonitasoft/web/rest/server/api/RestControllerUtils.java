/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.SpringWebConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Test-purpose utility class to initialize MockMvc for REST controllers with session attributes.
 */
public class RestControllerUtils {

    /**
     * Cached message converters, reusing the production SpringWebConfiguration
     * to ensure tests use the exact same converter setup as production.
     */
    private static final List<HttpMessageConverter<?>> MESSAGE_CONVERTERS = SpringWebConfiguration
            .createBonitaMessageConverters();

    public static MockMvc initMockMvcWithSessionAttributes(AbstractRESTController controller,
            Map<String, Object> sessionAttributes, APISession apiSession) {
        doReturn(apiSession).when(controller).getApiSession(any());
        sessionAttributes.put(SessionUtil.API_SESSION_PARAM_KEY, apiSession);

        // Match the production SpringWebConfiguration: default to JSON content type
        var contentNegotiationManager = new ContentNegotiationManager(
                new HeaderContentNegotiationStrategy(),
                new FixedContentNegotiationStrategy(MediaType.APPLICATION_JSON));

        StandaloneMockMvcBuilder builder = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new SpringRestResponseEntityExceptionHandler())
                .setContentNegotiationManager(contentNegotiationManager);

        builder.setMessageConverters(MESSAGE_CONVERTERS.toArray(new HttpMessageConverter<?>[0]));

        // Force UTF-8 response encoding so that MockHttpServletResponse.getContentAsString()
        // correctly decodes UTF-8 bytes (servlet spec defaults to ISO-8859-1)
        var utf8Filter = new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), false, true);
        builder.addFilter(utf8Filter);

        return builder.build();
    }

}
