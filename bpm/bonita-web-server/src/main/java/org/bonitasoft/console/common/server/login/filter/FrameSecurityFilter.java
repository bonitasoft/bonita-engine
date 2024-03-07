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
package org.bonitasoft.console.common.server.login.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.filter.ExcludingPatternFilter;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.preferences.properties.SecurityProperties;

/**
 * Security filter setting the X-Frame-Options in the response headers
 *
 * @author Anthony Birembaut
 */
public class FrameSecurityFilter extends ExcludingPatternFilter {

    protected static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";

    protected static final String X_FRAME_OPTIONS_HEADER_DEFAULT = "SAMEORIGIN";

    protected static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";

    protected static final String CONTENT_SECURITY_POLICY_HEADER_DEFAULT = "frame-ancestors 'self';";

    protected String xFrameHeaderValue;

    protected String contentSecurityHeaderValue;

    @Override
    public String getDefaultExcludedPages() {
        return "";
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        SecurityProperties securityProperties = getSecurityProperties();
        String xFrameHeaderPropertyValue = securityProperties.getXFrameOptionsHeader();
        xFrameHeaderValue = Objects.requireNonNullElse(xFrameHeaderPropertyValue,
                StringUtils.defaultIfEmpty(filterConfig.getInitParameter(X_FRAME_OPTIONS_HEADER),
                        X_FRAME_OPTIONS_HEADER_DEFAULT));
        String contentSecurityHeaderPropertyValue = securityProperties.getContentSecurityPolicyHeader();
        contentSecurityHeaderValue = Objects.requireNonNullElse(contentSecurityHeaderPropertyValue,
                StringUtils.defaultIfEmpty(filterConfig.getInitParameter(CONTENT_SECURITY_POLICY_HEADER),
                        CONTENT_SECURITY_POLICY_HEADER_DEFAULT));
        super.init(filterConfig);
    }

    protected SecurityProperties getSecurityProperties() {
        return PropertiesFactory.getSecurityProperties();
    }

    @Override
    public void proceedWithFiltering(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws ServletException, IOException {
        // casting to HTTPServlet(Request|Response)
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        // X-frame-options (ClickJacking)
        if (!StringUtils.isBlank(xFrameHeaderValue)) {
            res.setHeader(X_FRAME_OPTIONS_HEADER, xFrameHeaderValue);
        }
        if (!StringUtils.isBlank(contentSecurityHeaderValue)) {
            res.setHeader(CONTENT_SECURITY_POLICY_HEADER, contentSecurityHeaderValue);
        }

        chain.doFilter(req, res);
    }
}
