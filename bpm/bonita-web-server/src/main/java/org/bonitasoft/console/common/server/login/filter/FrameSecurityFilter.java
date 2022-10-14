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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.filter.ExcludingPatternFilter;

/**
 * Security filter setting the X-Frame-Options in the response headers
 *
 * @author Anthony Birembaut
 */
public class FrameSecurityFilter extends ExcludingPatternFilter {

    protected static final String X_FRAME_OPTIONS_HEADER = "X-Frame-Options";

    protected static final String FRAME_FILTER_EXCLUDED_PAGES_PATTERN = "^/(bonita/)?(portal/resource/.+/content/$)|(portal/custom-page/.+/$)";

    protected String headerValue;

    @Override
    public String getDefaultExcludedPages() {
        return FRAME_FILTER_EXCLUDED_PAGES_PATTERN;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        headerValue = StringUtils.defaultIfEmpty(filterConfig.getInitParameter(X_FRAME_OPTIONS_HEADER), "SAMEORIGIN");
        super.init(filterConfig);
    }

    @Override
    public void proceedWithFiltering(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws ServletException, IOException {
        // casting to HTTPServlet(Request|Response)
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        // X-frame-options (ClickJacking)
        res.setHeader(X_FRAME_OPTIONS_HEADER, headerValue);

        chain.doFilter(req, res);
    }
}
