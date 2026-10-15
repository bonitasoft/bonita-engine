/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.commons.exceptions;

import java.io.Serializable;
import java.util.Map;

public interface ExceptionContext {

    Map<SExceptionContext, Serializable> getContext();

    void setProcessDefinitionIdOnContext(final long id);

    void setProcessDefinitionNameOnContext(final String name);

    void setProcessDefinitionVersionOnContext(final String version);

    void setProcessInstanceIdOnContext(final long id);

    void setRootProcessInstanceIdOnContext(final long id);

    void setConnectorDefinitionIdOnContext(final String id);

    void setConnectorInputOnContext(String inputName);

    void setConnectorNameOnContext(String name);

    void setConnectorImplementationClassNameOnContext(final String name);

    void setConnectorDefinitionVersionOnContext(final String version);

    void setConnectorActivationEventOnContext(final String activationEvent);

    void setConnectorInstanceIdOnContext(final long id);

    void setFlowNodeDefinitionIdOnContext(final long id);

    void setFlowNodeInstanceIdOnContext(final long id);

    void setFlowNodeNameOnContext(final String name);

    void setMessageInstanceNameOnContext(final String name);

    void setMessageInstanceTargetProcessOnContext(final String name);

    void setMessageInstanceTargetFlowNodeOnContext(final String name);

    void setWaitingMessageEventTypeOnContext(final String eventType);

    void setDocumentIdOnContext(final long id);

    void setUserIdOnContext(final long userId);

    void setGroupIdOnContext(final long groupId);

    void setRoleIdOnContext(final long roleId);

}
