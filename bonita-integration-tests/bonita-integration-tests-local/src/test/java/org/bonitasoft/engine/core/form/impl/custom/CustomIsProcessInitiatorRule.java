/*
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.form.impl.custom;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.page.AuthorizationRule;
import org.bonitasoft.engine.page.AuthorizationRuleConstants;
import org.bonitasoft.engine.page.AuthorizationRuleWithParameters;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Laurent Leseigneur
 */
public class CustomIsProcessInitiatorRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private ProcessInstanceService processInstanceService;

    private SessionService sessionService;

    private SessionAccessor sessionAccessor;

    // autowired by spring
    public CustomIsProcessInitiatorRule(ProcessInstanceService processInstanceService, SessionService sessionService, SessionAccessor sessionAccessor) {
        this.processInstanceService = processInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public boolean isAllowed(String key, Map<String, Serializable> context) throws SExecutionException {
        //add business logic here
        return true;
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_PROCESS_INITIATOR + "_CUSTOM";
    }
}
