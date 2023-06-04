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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Haojie Yuan
 * @author Benjamin Parisel
 * @author Anthony Birembaut
 */
public class CacheFilter extends ExcludingPatternFilter {

    public static final String ALWAYS_CACHING = "alwaysCaching";

    public static final String NO_CUSTOMPAGE_CACHE = "noCacheCustomPage";

    protected static final String CACHE_FILTER_EXCLUDED_RESOURCES_PATTERN = "^/(bonita/)?(apps/.+/$)|(portal/resource/.+/content/$)|(portal/custom-page/.+/$)|(portal/custom-page/API/)";

    protected Map<String, String> paramMap = new HashMap<>();

    private final String DURATION = "duration";

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        final Enumeration<?> names = filterConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            final String value = filterConfig.getInitParameter(name);
            paramMap.put(name, value);
        }
    }

    public String getDefaultExcludedPages() {
        return CACHE_FILTER_EXCLUDED_RESOURCES_PATTERN;
    }

    @Override
    public void proceedWithFiltering(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        setResponseHeader(res);

        chain.doFilter(req, res);
    }

    @Override
    public void excludePatternFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");

        chain.doFilter(req, res);
    }

    private void setResponseHeader(final HttpServletResponse response) {
        final Integer duration = Integer.valueOf(paramMap.get(DURATION));
        final boolean alwaysCaching = Boolean.parseBoolean(paramMap.get(ALWAYS_CACHING));
        final boolean noCustomPageCache = Boolean.parseBoolean(System.getProperty(NO_CUSTOMPAGE_CACHE));
        if (!noCustomPageCache && alwaysCaching) {
            response.setHeader("Cache-Control", "max-age=" + duration.intValue());
        } else {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
        }
    }
}
