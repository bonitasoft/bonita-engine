/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.operation;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class BusinessDataOperation {

    protected final RefBusinessDataService refBusinessDataService;

    protected final FlowNodeInstanceService flowNodeInstanceService;

    protected BusinessDataOperation(final RefBusinessDataService refBusinessDataService, final FlowNodeInstanceService flowNodeInstanceService) {
        super();
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    protected SRefBusinessDataInstance getRefBusinessDataInstance(final String businessDataName, final long containerId, final String containerType)
            throws SFlowNodeNotFoundException, SFlowNodeReadException, SRefBusinessDataInstanceNotFoundException, SBonitaReadException {
        long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
        return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
    }

}
