/**
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
 **/
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Matthieu Chaffotte
 */
public class Login implements TransactionContentWithResult<SSession> {

    private final LoginService loginService;

    private final long tenantId;

    private final String userName;

    private final String password;

    private SSession sSession;

    public Login(final LoginService loginService, final long tenantId, final String userName, final String password) {
        super();
        this.loginService = loginService;
        this.tenantId = tenantId;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void execute() throws SBonitaException {
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_USERNAME, userName);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        sSession = loginService.login(credentials);
    }

    @Override
    public SSession getResult() {
        return sSession;
    }

}
