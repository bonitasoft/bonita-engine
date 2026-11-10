/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.rest.server.utils.BonitaJacksonModuleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

@Configuration
// Note: Do NOT use @EnableWebMvc when extending WebMvcConfigurationSupport directly
@ComponentScan({ "org.bonitasoft.web.rest.server.api", "com.bonitasoft.web.rest.server.api" })
public class SpringWebConfiguration extends WebMvcConfigurationSupport {

    @Bean
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping(
            @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
            @Qualifier("mvcConversionService") FormattingConversionService conversionService,
            @Qualifier("mvcResourceUrlProvider") ResourceUrlProvider resourceUrlProvider) {
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping(contentNegotiationManager,
                conversionService, resourceUrlProvider);
        handlerMapping.setAlwaysUseFullPath(true);
        return handlerMapping;
    }

    /**
     * Creates the list of HTTP message converters configured for the Bonita REST API.
     * <p>
     * Uses Spring's default converter ordering (where StringHttpMessageConverter comes
     * before MappingJackson2HttpMessageConverter), then replaces the default Jackson
     * converter with one configured with Bonita custom serializers (_string suffix fields
     * for numeric IDs, custom date/time formats).
     * <p>
     * This ordering is important: controllers returning pre-serialized JSON strings
     * (e.g. BusinessDataController, ProcessDefinitionDesignController) must be handled
     * by StringHttpMessageConverter to avoid double-serialization by Jackson.
     *
     * @return the configured message converters
     */
    public static List<HttpMessageConverter<?>> createBonitaMessageConverters() {
        var converters = new ArrayList<HttpMessageConverter<?>>();
        // Use a temporary instance to access the protected addDefaultHttpMessageConverters
        new SpringWebConfiguration().addDefaultHttpMessageConverters(converters);

        // Replace the default Jackson converter with one configured with Bonita custom serializers
        converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        ObjectMapper objectMapper = new ObjectMapper();
        BonitaJacksonModuleProvider.configureObjectMapper(objectMapper);
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        return converters;
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(createBonitaMessageConverters());
    }

    @Override
    protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // enforces JSON as the default content type for content negotiation for Spring MVC APIs
        // /!\ Make sure that RestControllerUtils uses the same configuration /!\
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }
}
