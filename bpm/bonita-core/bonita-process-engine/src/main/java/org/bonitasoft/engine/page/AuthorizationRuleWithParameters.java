/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * author Emmanuel Duchastenier
 */
public abstract class AuthorizationRuleWithParameters {

    protected Long getLongParameter(Map<String, Serializable> context, String parameterKey) {
        final Map<String, String[]> queryParameters = (Map<String, String[]>) context.get(URLAdapterConstants.QUERY_PARAMETERS);
        if (queryParameters != null) {
            String[] idParamValue = queryParameters.get(parameterKey);
            if (idParamValue != null && idParamValue.length > 0) {
                return Long.parseLong(idParamValue[0]);
            }
        }
        return null;
    }
    
    protected long getLoggedUserId(SessionAccessor sessionAccessor, SessionService sessionService) throws SExecutionException {
        try {
            return sessionService.getSession(sessionAccessor.getSessionId()).getUserId();
        } catch (SSessionNotFoundException e) {
            throw new SExecutionException(e);
        } catch (SessionIdNotSetException e) {
            throw new SExecutionException(e);
        }
    }
}
