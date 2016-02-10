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

/**
 * @author Hongwen Zang
 * @author Elias Ricken de Medeiros
 */
public class UpdateFlownodeInstance implements TransactionContent {

    private final long userId;

    private final long flowNodeInstanceId;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public UpdateFlownodeInstance(final long userId, final long flowNodeInstanceId, final FlowNodeInstanceService flowNodeInstanceService) {
        super();
        this.userId = userId;
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public void execute() throws SBonitaException {
        final SFlowNodeInstance activity = flowNodeInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        flowNodeInstanceService.setExecutedBy(activity, userId);
    }

}
