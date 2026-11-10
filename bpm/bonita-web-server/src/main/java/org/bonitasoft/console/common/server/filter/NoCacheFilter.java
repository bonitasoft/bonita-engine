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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * This Filter adds headers to the response to prevent caching of the page/resource.
 * It extends ExcludingPatternFilter, so that it benefits from its once-per-request behavior.
 *
 * @author Paul AMAR
 */
public class NoCacheFilter extends ExcludingPatternFilter {

    @Override
    public void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
        chain.doFilter(request, res);
    }

    @Override
    public String getDefaultExcludedPages() {
        return "";
    }
}
