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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
// Note: Do NOT use @EnableWebMvc when extending WebMvcConfigurationSupport directly
@ComponentScan({ "org.bonitasoft.web.rest.server.api", "com.bonitasoft.web.rest.server.api" })
public class SpringWebConfiguration extends WebMvcConfigurationSupport {

    /**
     * Forces path matching on the <b>full</b> request path (context path excluded), instead of the
     * path relative to the servlet mapping.
     * <p>
     * This is required because all our controllers declare absolute patterns, prefix included
     * (e.g. {@code @RequestMapping("/API/form/mapping")}), while the {@code SpringRest}
     * DispatcherServlet is mapped in web.xml with a mix of exact patterns (e.g.
     * {@code /API/bpm/processName}) and path-prefix patterns (e.g. {@code /API/system/log/*},
     * {@code /APISpringInternal/*}).
     * <p>
     * Since Spring Framework 6, {@code AbstractHandlerMapping} uses a {@link PathPatternParser} by
     * default, and with parsed patterns the lookup path is computed by
     * {@code ServletRequestPathUtils}, which folds the servlet path into the context path for every
     * servlet mapping matching by path prefix ({@code MappingMatch.PATH}). The
     * {@code /API/...} prefix would then be stripped from the lookup path and no controller pattern
     * could ever match a request served through a prefix mapping.
     * <p>
     * Configuring a {@link UrlPathHelper} here switches the handler mappings back to
     * {@code AntPathMatcher} with full-path matching (see
     * {@code PathMatchConfigurer#preferPathMatcher()}). Note that calling
     * {@code setAlwaysUseFullPath(true)} directly on the {@code RequestMappingHandlerMapping} bean
     * would <b>not</b> work: it only configures the legacy {@code UrlPathHelper} without clearing
     * the parsed-pattern parser, which keeps taking precedence.
     * <p>
     * <b>This is the legacy path-matching mode.</b> It is fully supported in Spring Framework 6.2,
     * but Spring Framework 7.0 deprecates {@code PathMatchConfigurer#setUrlPathHelper} for removal:
     * a Spring 7 upgrade must drop the servlet prefix from the controller mappings and map the
     * {@code SpringRest} servlet on a single path prefix instead, so that parsed patterns can be
     * used as designed. See BPA-807.
     */
    @Override
    protected void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setAlwaysUseFullPath(true);
        configurer.setUrlPathHelper(urlPathHelper);
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
