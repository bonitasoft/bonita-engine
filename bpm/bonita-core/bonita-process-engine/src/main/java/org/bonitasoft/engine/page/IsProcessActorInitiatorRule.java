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

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.xml.ActorMapping;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * author Anthony Birembaut
 */
public class IsProcessActorInitiatorRule implements AuthorizationRule {

    ActorMappingService actorMappingService;
    
    SessionAccessor sessionAccessor;
    
    SessionService sessionService;
    
    public IsProcessActorInitiatorRule(ActorMappingService actorMappingService, SessionAccessor sessionAccessor, SessionService sessionService) {
        this.actorMappingService = actorMappingService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public boolean isAllowed(Map<String, Serializable> context) throws SExecutionException {
        @SuppressWarnings("unchecked")
        final Map<String, String[]> queryParameters = (Map<String, String[]>) context.get(URLAdapterConstants.QUERY_PARAMETERS);
        String[] idParamValue = new String[0];
        if(queryParameters != null){
            idParamValue = queryParameters.get(URLAdapterConstants.ID_QUERY_PARAM);
        }
        long processDefinitionId;
        if (idParamValue == null || idParamValue.length == 0) {
            throw new IllegalArgumentException("The parameter \"id\" is missing from the original URL");
        } else {
            processDefinitionId = Long.parseLong(idParamValue[0]);
            try {
                final long userId = sessionService.getSession(sessionAccessor.getSessionId()).getUserId();
                return actorMappingService.canUserStartProcessDefinition(userId, processDefinitionId);
            } catch (final SBonitaException e) {
                throw new SExecutionException("Unable to figure out if the logged user is an actor initiator for the process " + processDefinitionId, e);
            }
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_PROCESS_OWNER;
    }
}
