/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.FLOWNODE;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.PROCESS;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessInstanceRecoveryService {

    private final FlowNodeInstanceService flowNodeInstanceService;
    private final ProcessInstanceService processInstanceService;
    private final ExecuteFlowNodes executeFlowNodes;
    private final ExecuteProcesses executeProcesses;
    private int readBatchSize;
    private Duration considerElementsOlderThan;

    public ProcessInstanceRecoveryService(FlowNodeInstanceService flowNodeInstanceService,
            ProcessInstanceService processInstanceService,
            ExecuteFlowNodes executeFlowNodes, ExecuteProcesses executeProcesses) {
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processInstanceService = processInstanceService;
        this.executeFlowNodes = executeFlowNodes;
        this.executeProcesses = executeProcesses;
    }

    @Value("${bonita.tenant.recover.read_batch_size:5000}")
    public void setReadBatchSize(int readBatchSize) {
        this.readBatchSize = readBatchSize;
    }

    @Value("${bonita.tenant.recover.consider_elements_older_than:PT1H}")
    public void setConsiderElementsOlderThan(String considerElementsOlderThan) {
        setConsiderElementsOlderThan(Duration.parse(considerElementsOlderThan));
    }

    @VisibleForTesting
    void setConsiderElementsOlderThan(Duration considerElementsOlderThan) {
        this.considerElementsOlderThan = considerElementsOlderThan;
    }

    /**
     * Retrieve elements ( ProcessInstance and Flow Nodes ) that needs to be recovered and that are older than the given
     * duration.
     *
     * @param considerElementsOlderThan consider elements older than that duration
     * @return elements to be recovered
     */
    public List<ElementToRecover> getAllElementsToRecover(Duration considerElementsOlderThan) {
        List<ElementToRecover> elementsToRecover;
        try {
            elementsToRecover = getAllProcessInstancesToRecover(considerElementsOlderThan);
            elementsToRecover.addAll(getAllFlowNodeInstancesToRecover(considerElementsOlderThan));
            return elementsToRecover;
        } catch (SBonitaException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Trigger works to execute elements ( ProcessInstance and Flow Nodes ) that needs to be recovered
     *
     * @param elementsToRecover elements needs to be recovered
     */
    public void recover(List<ElementToRecover> elementsToRecover) {
        executeFlowNodes.executeFlowNodes(elementsToRecover.stream()
                .filter(e -> e.getType() == FLOWNODE)
                .map(ElementToRecover::getId)
                .collect(Collectors.toList()));
        executeProcesses.execute(elementsToRecover.stream()
                .filter(e -> e.getType() == PROCESS)
                .map(ElementToRecover::getId)
                .collect(Collectors.toList()));
    }

    /**
     * Recover all elements considered as "stuck".
     * Only recover elements older than a duration configured with {@link #setConsiderElementsOlderThan(String)}.
     */
    public void recoverAllElements() {
        List<ElementToRecover> allElementsToRecover = getAllElementsToRecover(considerElementsOlderThan);
        recover(allElementsToRecover);
    }

    private List<ElementToRecover> getAllFlowNodeInstancesToRecover(Duration considerElementsOlderThan)
            throws SBonitaException {
        List<Long> flownodesToRestart = new ArrayList<>();
        // using a too low page size (100) causes too many access to the database and causes timeout exception if there are lot of elements.
        // As we retrieve only the id we can use a greater page size
        QueryOptions queryOptions = new QueryOptions(0, readBatchSize);
        List<Long> ids;
        log.info("Start detecting flow nodes to restart...");
        do {
            ids = flowNodeInstanceService.getFlowNodeInstanceIdsToRecover(considerElementsOlderThan, queryOptions);
            flownodesToRestart.addAll(ids);
            queryOptions = QueryOptions.getNextPage(queryOptions);
        } while (ids.size() == queryOptions.getNumberOfResults());
        log.info("Found {} flow nodes to restart", flownodesToRestart.size());
        return flownodesToRestart
                .stream()
                .map(id -> ElementToRecover.builder().id(id).type(FLOWNODE).build())
                .collect(Collectors.toList());
    }

    private List<ElementToRecover> getAllProcessInstancesToRecover(Duration considerElementsOlderThan)
            throws SBonitaException {
        final List<Long> ids = new ArrayList<>();
        QueryOptions queryOptions = new QueryOptions(0, readBatchSize);
        List<Long> processInstancesIds;
        do {
            processInstancesIds = processInstanceService.getProcessInstanceIdsToRecover(considerElementsOlderThan,
                    queryOptions);
            queryOptions = QueryOptions.getNextPage(queryOptions);
            ids.addAll(processInstancesIds);
        } while (processInstancesIds.size() == queryOptions.getNumberOfResults());
        return ids
                .stream().map(id -> ElementToRecover.builder().id(id).type(PROCESS).build())
                .collect(Collectors.toList());
    }

}
