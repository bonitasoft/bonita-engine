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
package org.bonitasoft.console.common.server.filter;

import java.net.URI;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

public class URLExcludePattern {

    /**
     * the Pattern of url not to filter
     */
    public final Pattern excludePattern;

    private static final Logger LOGGER = LoggerFactory.getLogger(URLExcludePattern.class.getName());

    public URLExcludePattern(final FilterConfig filterConfig, String defaultExcludePattern) {
        final String contextPath = filterConfig.getServletContext().getContextPath();
        String processedDefaultExcludePattern;
        if (contextPath.length() > 0 && !contextPath.equals("/bonita")) {
            String webappName = contextPath.substring(1);
            processedDefaultExcludePattern = defaultExcludePattern.replace("bonita", webappName);
        } else {
            processedDefaultExcludePattern = defaultExcludePattern;
        }
        final String configExcludePattern = filterConfig.getInitParameter("excludePattern");
        excludePattern = compilePattern(
                StringUtils.defaultString(configExcludePattern, processedDefaultExcludePattern));
    }

    protected Pattern compilePattern(final String stringPattern) {
        if (StringUtils.isNotBlank(stringPattern)) {
            try {
                return Pattern.compile(stringPattern);
            } catch (final Exception e) {
                LOGGER.error("impossible to create pattern from [ {} ]  : ", stringPattern, e);
            }
        }
        return null;
    }

    /**
     * check the given url against the local url exclude pattern
     *
     * @param url the url to check
     * @return true if the url match the pattern
     */
    public boolean matchExcludePatterns(final String url) {
        if (getExcludePattern() == null) {
            return false;
        }
        try {
            // URL-decode then strip path parameters (semicolons) to handle both
            // literal ';' and percent-encoded '%3b' before normalization, preventing
            // ..;-based traversal from fooling the exclude pattern check.
            // URI handles both absolute (http://host/path) and relative (/path) references.
            URI uri = new URI(url);
            String rawPath = uri.getRawPath();
            if (rawPath == null) {
                rawPath = url;
            }
            String decodedPath = java.net.URLDecoder.decode(rawPath, "UTF-8");
            String sanitizedPath = PathSanitizer.stripPathParameters(decodedPath);
            // Re-encode the sanitized path before creating the URI for normalization,
            // because URL-decoding may have introduced literal spaces (from %20) that
            // are illegal in URI syntax per RFC 3986.
            String normalizedPath = new URI(UriUtils.encodePath(sanitizedPath, "UTF-8"))
                    .normalize().getPath();
            boolean isExcluded = getExcludePattern().matcher(sanitizedPath).find()
                    && getExcludePattern().matcher(normalizedPath).find();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exclude pattern {} with this url: {}",
                        isExcluded ? "match" : "does not match", url);
            }
            return isExcluded;
        } catch (final Exception e) {
            LOGGER.warn("impossible to get URL from given input [{}]: {}", url, e);
            return false;
        }
    }

    public Pattern getExcludePattern() {
        return excludePattern;
    }
}
