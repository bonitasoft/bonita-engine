/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.data;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SSubProcessActivityInstanceBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.SDataInstance;

/**
 * @author Feng Hui
 */
public class CreateSDataInstances implements TransactionContent {

    private final List<SDataInstance> sDataInstances;

    private final DataInstanceService dataInstanceService;

    private final SProcessInstance processInstance;

    private final ActivityInstanceService activityInstanceService;

    private final BPMInstanceBuilders instanceBuilders;

    private final SProcessDefinition processDefinition;

    public CreateSDataInstances(final List<SDataInstance> sDataInstances, final DataInstanceService dataInstanceService,
            final SProcessInstance processInstance, final ActivityInstanceService activityInstanceService, final BPMInstanceBuilders instanceBuilders,
            final SProcessDefinition processDefinition) {
        this.sDataInstances = sDataInstances;
        this.dataInstanceService = dataInstanceService;
        this.processInstance = processInstance;
        this.activityInstanceService = activityInstanceService;
        this.instanceBuilders = instanceBuilders;
        this.processDefinition = processDefinition;
    }

    @Override
    public void execute() throws SBonitaException {
        if (!sDataInstances.isEmpty()) {
            for (final SDataInstance sDataInstance : sDataInstances) {
                dataInstanceService.createDataInstance(sDataInstance);
            }
        }

        final boolean parentHasData = !processDefinition.getProcessContainer().getDataDefinitions().isEmpty();
        if (!sDataInstances.isEmpty() || parentHasData) {
            if (processInstance.getCallerId() > 0) {
                final SFlowNodeInstance caller = activityInstanceService.getFlowNodeInstance(processInstance.getCallerId());
                if (SFlowNodeType.SUB_PROCESS.equals(caller.getType())) {
                    final SSubProcessActivityInstanceBuilder keyProvider = instanceBuilders.getSSubProcessActivityInstanceBuilder();
                    dataInstanceService.addChildContainer(caller.getLogicalGroup(keyProvider.getParentProcessInstanceIndex()),
                            DataInstanceContainer.PROCESS_INSTANCE.name(), processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name());
                } else {
                    dataInstanceService.createDataContainer(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name());
                }
            } else {
                dataInstanceService.createDataContainer(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name());
            }
        }
    }
}
