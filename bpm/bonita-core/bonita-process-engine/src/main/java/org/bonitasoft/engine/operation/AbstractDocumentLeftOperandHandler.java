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
 */
package org.bonitasoft.engine.operation;

import java.util.List;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractDocumentLeftOperandHandler implements LeftOperandHandler {

    private final ActivityInstanceService activityInstanceService;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;

    public AbstractDocumentLeftOperandHandler(final ActivityInstanceService activityInstanceService, final SessionAccessor sessionAccessor,
                                              final SessionService sessionService) {
        this.activityInstanceService = activityInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    protected long getProcessInstanceId(final long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        long processInstanceId;
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            processInstanceId = containerId;
        } else {
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
            processInstanceId = flowNodeInstance.getParentProcessInstanceId();
        }
        return processInstanceId;
    }

    protected long getAuthorId(long containerId, String containerType) {
        long loggedUserFromSession = sessionService.getLoggedUserFromSession(sessionAccessor);
        try {
            if (loggedUserFromSession <= 0 && DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(containerType)) {
                SActivityInstance activityInstance = activityInstanceService.getActivityInstance(containerId);
                if (activityInstance instanceof SHumanTaskInstance) {
                    SHumanTaskInstance instance = (SHumanTaskInstance) activityInstance;
                    return instance.getAssigneeId();
                }
            }
        } catch (SActivityInstanceNotFoundException | SActivityReadException ignored) {
        }
        return loggedUserFromSession;
    }

    @Override
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand,final long leftOperandContainerId, final String leftOperandContainerType, final SExpressionContext expressionContext) {
        //do nothing
    }

    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand,final long leftOperandContainerId, final String leftOperandContainerType, final SExpressionContext expressionContext)
            throws SBonitaReadException {
        //do nothing
    }
}
