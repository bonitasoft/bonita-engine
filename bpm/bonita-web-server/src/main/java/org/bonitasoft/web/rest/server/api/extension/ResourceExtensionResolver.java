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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.page.PageMappingService;
import org.bonitasoft.console.common.server.page.PageReference;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.restlet.Request;
import org.restlet.ext.servlet.ServletUtils;

/**
 * @author Laurent Leseigneur
 */
public class ResourceExtensionResolver {

    public static final String MAPPING_KEY_SEPARATOR = "|";
    public static final String MAPPING_KEY_PREFIX = "apiExtension";
    public static final String API_EXTENSION_TEMPLATE_PREFIX = "/API/extension/";
    private final Request request;
    private final PageMappingService pageMappingService;

    public ResourceExtensionResolver(Request request, PageMappingService pageMappingService) {
        this.request = request;
        this.pageMappingService = pageMappingService;
    }

    public Long resolvePageId(APISession apiSession) throws BonitaException {
        final HttpServletRequest httpServletRequest = getHttpServletRequest();
        final PageReference pageReference;
        pageReference = pageMappingService.getPage(httpServletRequest, apiSession, generateMappingKey(),
                httpServletRequest.getLocale(), false);
        return pageReference.getPageId();
    }

    protected HttpServletRequest getHttpServletRequest() {
        return ServletUtils.getRequest(this.request);
    }

    public String generateMappingKey() {
        final StringBuilder builder = new StringBuilder();
        final String requestAsString = getHttpServletRequest().getRequestURI();
        final String pathTemplate = StringUtils.substringAfter(requestAsString, API_EXTENSION_TEMPLATE_PREFIX);

        builder.append(MAPPING_KEY_PREFIX)
                .append(MAPPING_KEY_SEPARATOR)
                .append(request.getMethod().getName())
                .append(MAPPING_KEY_SEPARATOR)
                .append(pathTemplate);

        return builder.toString();
    }

    public ControllerClassName resolveRestApiControllerClassName(PageResourceProviderImpl pageResourceProvider)
            throws NotFoundException {
        final Properties properties = new Properties();

        try (final InputStream resourceAsStream = pageResourceProvider.getResourceAsStream("page.properties")) {
            properties.load(resourceAsStream);
        } catch (final IOException e) {
            throw new NotFoundException("error while getting resource:" + generateMappingKey());
        }

        final String apiExtensionList = (String) properties.get("apiExtensions");
        final String[] apiExtensions = apiExtensionList.split(",");

        return findMatchingControllerClassName(properties, apiExtensions);
    }

    /**
     * return the content of the property 'className' or 'classFileName' (if className isn't present).
     * - 'className' refers to the qualified name of the main class. If 'className' is present then it indicates that
     * the jar
     * file
     * containing the extension is present.
     * - 'classFileName' is the path to the groovy source file (lagacy mode), it indicates that the rest api needs to be
     * compiled
     * at runtime with a groovy compiler.
     */
    private ControllerClassName findMatchingControllerClassName(Properties properties, String[] apiExtensions)
            throws NotFoundException {
        for (final String apiExtension : apiExtensions) {
            final String method = (String) properties.get(String.format("%s.method", apiExtension.trim()));
            final String pathTemplate = (String) properties.get(String.format("%s.pathTemplate", apiExtension.trim()));
            final String className = (String) properties.get(String.format("%s.className", apiExtension.trim()));
            final String classFileName = (String) properties
                    .get(String.format("%s.classFileName", apiExtension.trim()));

            if (extensionMatches(method, pathTemplate)) {
                return className != null && !className.isEmpty()
                        ? new ControllerClassName(className, false)
                        : new ControllerClassName(classFileName, true);
            }
        }
        throw new NotFoundException("error while getting resource:" + generateMappingKey());
    }

    private boolean extensionMatches(String method, String pathTemplate) {
        return request.getMethod().getName().equals(method)
                && getHttpServletRequest().getRequestURI()
                        .endsWith(String.format("%s%s", API_EXTENSION_TEMPLATE_PREFIX, pathTemplate));
    }
}
