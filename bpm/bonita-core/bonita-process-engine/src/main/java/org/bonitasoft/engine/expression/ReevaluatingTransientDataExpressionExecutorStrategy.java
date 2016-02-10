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
package org.bonitasoft.engine.expression;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.data.instance.impl.TransientDataExpressionExecutorStrategy;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.TransientDataLeftOperandHandler;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public class ReevaluatingTransientDataExpressionExecutorStrategy extends TransientDataExpressionExecutorStrategy {

    private FlowNodeInstanceService flownodeInstanceService;

    private ProcessDefinitionService processDefinitionService;

    private BPMInstancesCreator bpmInstancesCreator;

    private final TransientDataService transientDataService;

    private final TechnicalLoggerService logger;

    /**
     * @param transientDataService
     */
    public ReevaluatingTransientDataExpressionExecutorStrategy(final TransientDataService transientDataService, final TechnicalLoggerService logger) {
        super(transientDataService);
        this.transientDataService = transientDataService;
        this.logger = logger;

    }

    public void setFlownodeInstanceService(final FlowNodeInstanceService flownodeInstanceService) {
        this.flownodeInstanceService = flownodeInstanceService;
    }

    public void setProcessDefinitionService(final ProcessDefinitionService processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    public void setBpmInstancesCreator(final BPMInstancesCreator bpmInstancesCreator) {
        this.bpmInstancesCreator = bpmInstancesCreator;
    }

    @Override
    protected SDataInstance handleDataNotFound(final String name, final long containerId, final String containerType, final SDataInstanceNotFoundException e)
            throws SBonitaReadException, SDataInstanceException {
        logger.log(getClass(), TechnicalLogSeverity.WARNING, "The value of the transient data " + name + " of " + containerId + " " + containerType);
        TransientDataLeftOperandHandler
                .reevaluateTransientData(name, containerId, containerType, flownodeInstanceService, processDefinitionService, bpmInstancesCreator);
        return transientDataService.getDataInstance(name, containerId, containerType);
    }
}
