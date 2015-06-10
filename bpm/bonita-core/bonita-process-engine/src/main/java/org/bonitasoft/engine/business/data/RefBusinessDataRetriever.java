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

package org.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.operation.BusinessDataContext;

/**
 * @author Elias Ricken de Medeiros
 */
public class RefBusinessDataRetriever {

    private RefBusinessDataService refBusinessDataService;
    private final FlowNodeInstanceService flowNodeInstanceService;

    public RefBusinessDataRetriever(final RefBusinessDataService refBusinessDataService, FlowNodeInstanceService flowNodeInstanceService) {
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    public SRefBusinessDataInstance getRefBusinessDataInstance(BusinessDataContext context) throws SBonitaException {
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(context.getContainer().getType())) {
            return refBusinessDataService.getRefBusinessDataInstance(context.getName(), context.getContainer().getId());
        }
        try {
            return refBusinessDataService.getFlowNodeRefBusinessDataInstance(context.getName(), context.getContainer().getId());
        } catch (final SBonitaException sbe) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(context.getContainer().getId(), context.getContainer().getType());
            return refBusinessDataService.getRefBusinessDataInstance(context.getName(), processInstanceId);
        }
    }

}
