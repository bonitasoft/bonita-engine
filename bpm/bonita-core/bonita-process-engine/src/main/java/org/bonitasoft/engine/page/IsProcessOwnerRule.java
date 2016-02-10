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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;

/**
 * author Emmanuel Duchastenier, Anthony Birembaut
 */
public class IsProcessOwnerRule implements AuthorizationRule {

    SupervisorMappingService supervisorMappingService;

    SessionAccessor sessionAccessor;

    SessionService sessionService;

    FormMappingService formMappingService;

    public IsProcessOwnerRule(SupervisorMappingService supervisorMappingService, SessionAccessor sessionAccessor, SessionService sessionService,
            FormMappingService formMappingService) {
        this.supervisorMappingService = supervisorMappingService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.formMappingService = formMappingService;
    }

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) throws SExecutionException {
        try {
            SFormMapping formMapping = formMappingService.get(key);
            long processDefinitionId = formMapping.getProcessDefinitionId();
            final long userId = sessionService.getSession(sessionAccessor.getSessionId()).getUserId();
            return supervisorMappingService.isProcessSupervisor(processDefinitionId, userId);
        } catch (final SBonitaException e) {
            throw new SExecutionException("Unable to figure out if the logged user is a process owner.", e);
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_PROCESS_OWNER;
    }
}
