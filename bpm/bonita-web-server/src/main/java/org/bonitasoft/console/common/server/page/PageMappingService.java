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
package org.bonitasoft.console.common.server.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.page.URLAdapterConstants;
import org.bonitasoft.engine.session.APISession;

public class PageMappingService {

    public PageReference getPage(final HttpServletRequest request, final APISession apiSession, final String mappingKey,
            final Locale locale,
            final boolean executeAuthorizationRules) throws BonitaException {
        final Map<String, Serializable> context = new HashMap<>();
        //clone the request parameters map to a HashMap to avoid deserialization exceptions when calling a remote engine
        //see BS-16992 (the parameters map implementation is specific to the servlet container).
        Map<String, String[]> parametersMapCopy = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().clone()));
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) parametersMapCopy);
        context.put(URLAdapterConstants.LOCALE, locale.toString());
        context.put(URLAdapterConstants.CONTEXT_PATH, request.getContextPath());
        final PageAPI pageAPI = getPageAPI(apiSession);
        final PageURL pageURL = pageAPI.resolvePageOrURL(mappingKey, context, executeAuthorizationRules);
        return new PageReference(pageURL.getPageId(), pageURL.getUrl());
    }

    protected PageAPI getPageAPI(final APISession apiSession) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return TenantAPIAccessor.getCustomPageAPI(apiSession);
    }
}
