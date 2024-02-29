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
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Birembaut
 */
public abstract class ExcludingPatternFilter implements Filter {

    protected URLExcludePattern urlExcludePattern;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        urlExcludePattern = new URLExcludePattern(filterConfig, getDefaultExcludedPages());
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final String url = ((HttpServletRequest) request).getRequestURL().toString();
        if (matchExcludePatterns(url)) {
            excludePatternFiltering(request, response, chain);
        } else {
            proceedWithFiltering(request, response, chain);
        }
    }

    public void excludePatternFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public abstract void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException;

    public abstract String getDefaultExcludedPages();

    public Pattern compilePattern(final String stringPattern) {
        return urlExcludePattern.compilePattern(stringPattern);
    }

    /**
     * check the given url against the local url exclude pattern
     *
     * @param url
     *        the url to check
     * @return true if the url match the pattern
     */
    public boolean matchExcludePatterns(final String url) {
        return urlExcludePattern.matchExcludePatterns(url);
    }

    public Pattern getExcludePattern() {
        return urlExcludePattern.getExcludePattern();
    }

}
