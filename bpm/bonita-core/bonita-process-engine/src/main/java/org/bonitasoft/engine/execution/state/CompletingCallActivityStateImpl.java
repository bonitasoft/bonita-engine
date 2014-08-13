/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.state;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.document.api.DocumentMappingService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class CompletingCallActivityStateImpl extends CompletingActivityStateImpl {

    private final OperationService operationService;

    private final ProcessInstanceService processInstanceService;

    private final DataInstanceService dataInstanceService;

    private final DocumentMappingService documentMappingService;

    private final TechnicalLoggerService logger;

    private final ArchiveService archiveService;

    private final SCommentService commentService;

    private final ProcessDefinitionService processDeifnitionService;

    private final ConnectorInstanceService connectorInstanceService;

    public CompletingCallActivityStateImpl(final StateBehaviors stateBehaviors, final OperationService operationService,
            final ProcessInstanceService processInstanceService, final DataInstanceService dataInstanceService,
            final DocumentMappingService documentMappingService, final TechnicalLoggerService logger,
            final ArchiveService archiveService, final SCommentService commentService,
            final ProcessDefinitionService processDeifnitionService, final ConnectorInstanceService connectorInstanceService) {
        super(stateBehaviors);
        this.operationService = operationService;
        this.processInstanceService = processInstanceService;
        this.dataInstanceService = dataInstanceService;
        this.documentMappingService = documentMappingService;
        this.logger = logger;
        this.archiveService = archiveService;
        this.commentService = commentService;
        this.processDeifnitionService = processDeifnitionService;
        this.connectorInstanceService = connectorInstanceService;
    }

    @Override
    protected void onEnterToOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        executeDataOutputOperations(processDefinition, flowNodeInstance);
        stateBehaviors.executeOperations(processDefinition, (SActivityInstance) flowNodeInstance);
    }

    private void executeDataOutputOperations(final SProcessDefinition processDefinition, final SFlowNodeInstance instance)
            throws SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SCallActivityDefinition callActivityDef = (SCallActivityDefinition) processContainer.getFlowNode(instance.getFlowNodeDefinitionId());

        try {
            final SProcessInstance childProcInst = processInstanceService.getChildOfActivity(instance.getId());
            final SExpressionContext expressionContext = new SExpressionContext(childProcInst.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                    childProcInst.getProcessDefinitionId());
            for (final SOperation operation : callActivityDef.getDataOutputOperations()) {
                operationService.execute(operation, instance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), expressionContext);
            }
            // archive child process instance
            ProcessArchiver.archiveProcessInstance(childProcInst, archiveService, processInstanceService, dataInstanceService, documentMappingService, logger,
                    commentService, processDeifnitionService, connectorInstanceService);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    @Override
    public int getId() {
        return 30;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        return super.execute(processDefinition, flowNodeInstance);
    }

}
