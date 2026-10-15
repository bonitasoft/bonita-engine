/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.core.process.instance.api.BPMFailureService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;

/**
 * @author Baptiste Mesta
 */
public class SetInFailCallable implements Callable<Void> {

    private final FailedStateSetter failedStateSetter;
    private final SFlowNodeInstance flowNodeInstance;
    private final BPMFailureService bpmFailureService;
    private final BPMFailureService.Failure failure;

    SetInFailCallable(FailedStateSetter failedStateSetter, final SFlowNodeInstance flowNodeInstance,
            BPMFailureService bpmFailureService, BPMFailureService.Failure failure) {
        this.failedStateSetter = failedStateSetter;
        this.flowNodeInstance = flowNodeInstance;
        this.bpmFailureService = bpmFailureService;
        this.failure = failure;
    }

    @Override
    public Void call() throws Exception {
        failedStateSetter.setAsFailed(flowNodeInstance.getId());
        bpmFailureService.createFlowNodeFailure(flowNodeInstance, failure);
        return null;
    }

}
