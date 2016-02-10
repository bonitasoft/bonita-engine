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
package org.bonitasoft.engine.api.impl.transaction.flownode;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Elias Ricken de Medeiros
 */
public class SetFlowNodeStateCategory implements TransactionContent {

    private final long activityInstanceId;

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final SStateCategory stateCategory;

    public SetFlowNodeStateCategory(final FlowNodeInstanceService flowNodeInstanceService, final long activityInstanceId,
            final SStateCategory stateCategory) {
        this.activityInstanceId = activityInstanceId;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.stateCategory = stateCategory;
    }

    @Override
    public void execute() throws SBonitaException {
        final SFlowNodeInstance flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(activityInstanceId);
        flowNodeInstanceService.setStateCategory(flowNodeInstance, stateCategory);
    }

}
