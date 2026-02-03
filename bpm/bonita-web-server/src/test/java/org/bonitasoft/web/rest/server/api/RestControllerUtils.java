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

import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.utils.BonitaJacksonModuleProvider;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Test-purpose utility class to initialize MockMvc for REST controllers with session attributes.
 */
public class RestControllerUtils {

    /**
     * Cached default HTTP message converters configured with Bonita custom serializers.
     */
    private static final List<HttpMessageConverter<?>> MESSAGE_CONVERTERS = createConfiguredConverters();

    public static MockMvc initMockMvcWithSessionAttributes(AbstractRESTController controller,
            Map<String, Object> sessionAttributes, APISession apiSession) {
        doReturn(apiSession).when(controller).getApiSession(any());
        sessionAttributes.put(SessionUtil.API_SESSION_PARAM_KEY, apiSession);

        StandaloneMockMvcBuilder builder = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new SpringRestResponseEntityExceptionHandler());

        // Configure Jackson converter with Bonita custom serializers while keeping other default converters
        configureJacksonConverter(builder);

        return builder.build();
    }

    /**
     * Configures the Jackson message converter with Bonita custom serializers.
     * Uses setMessageConverters to add our configured converter while keeping defaults.
     */
    private static void configureJacksonConverter(StandaloneMockMvcBuilder builder) {
        builder.setMessageConverters(MESSAGE_CONVERTERS.toArray(new HttpMessageConverter<?>[0]));
    }

    /**
     * Creates HTTP message converters configured with Bonita custom serializers.
     */
    private static List<HttpMessageConverter<?>> createConfiguredConverters() {
        List<HttpMessageConverter<?>> converters = new WebMvcConfigurationSupport() {

            public List<HttpMessageConverter<?>> getDefaultConverters() {
                return getMessageConverters();
            }
        }.getDefaultConverters();

        // Configure the Jackson converter with Bonita custom serializers
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                BonitaJacksonModuleProvider.configureObjectMapper(jacksonConverter.getObjectMapper());
                break;
            }
        }

        return converters;
    }

}
