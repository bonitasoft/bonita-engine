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

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.rest.server.utils.BonitaJacksonModuleProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
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
     * Configures HTTP message converters with custom Bonita Jackson serializer.
     * This method is called by Spring during context initialization to set up
     * the message converters for REST API serialization/deserialization.
     */
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // First, create a custom Jackson converter with Bonita serializers
        // for backward compatibility with Restlet JSON format (_string suffix fields)
        ObjectMapper objectMapper = new ObjectMapper();
        BonitaJacksonModuleProvider.configureObjectMapper(objectMapper);
        MappingJackson2HttpMessageConverter bonitaConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        // Add our custom converter first (highest priority)
        converters.add(bonitaConverter);

        // Then add all default converters
        addDefaultHttpMessageConverters(converters);
    }
}
