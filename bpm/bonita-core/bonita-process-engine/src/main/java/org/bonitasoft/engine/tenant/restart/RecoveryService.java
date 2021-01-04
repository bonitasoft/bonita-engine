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

import static org.bonitasoft.engine.commons.CollectionUtil.split;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.FLOWNODE;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.PROCESS;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Responsible to recover from incidents like database or network outage.
 * It scans the database (on-demand) and reschedules the elements to recover.
 * It will recover these elements using multiple transaction using a batch size configured by the property
 * `bonita.tenant.work.batch_restart_size`
 */
@Component
@Slf4j
public class RecoveryService {

    public static final String DURATION_OF_RECOVERY_TASK = "bonita.bpmengine.recovery.duration";
    public static final String NUMBER_OF_RECOVERY = "bonita.bpmengine.recovery.execution";
    public static final String NUMBER_OF_ELEMENTS_RECOVERED_LAST_RECOVERY = "bonita.bpmengine.recovery.recovered.last";
    public static final String NUMBER_OF_ELEMENTS_RECOVERED_TOTAL = "bonita.bpmengine.recovery.recovered.total";

    private final FlowNodeInstanceService flowNodeInstanceService;
    private final ProcessInstanceService processInstanceService;
    private final UserTransactionService userTransactionService;
    private final FlowNodesRecover flowNodesRecover;
    private final ProcessesRecover processesRecover;
    private final SessionAccessor sessionAccessor;
    private final ObjectFactory<RecoveryMonitor> recoveryMonitorProvider;
    private final MeterRegistry meterRegistry;
    private long tenantId;
    private int readBatchSize;
    private int batchRestartSize;
    private Duration considerElementsOlderThan;
    private LongTaskTimer longTaskTimer;
    private Counter numberOfElementsRecoveredTotal;
    private Counter numberOfRecoverExecuted;
    private final AtomicLong numberOfElementsRecoveredDuringTheLastRecover = new AtomicLong();

    public RecoveryService(FlowNodeInstanceService flowNodeInstanceService,
            ProcessInstanceService processInstanceService,
            UserTransactionService userTransactionService,
            FlowNodesRecover flowNodesRecover,
            ProcessesRecover processesRecover,
            SessionAccessor sessionAccessor,
            ObjectFactory<RecoveryMonitor> recoveryMonitorProvider,
            MeterRegistry meterRegistry) {
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processInstanceService = processInstanceService;
        this.userTransactionService = userTransactionService;
        this.flowNodesRecover = flowNodesRecover;
        this.processesRecover = processesRecover;
        this.sessionAccessor = sessionAccessor;
        this.recoveryMonitorProvider = recoveryMonitorProvider;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    protected void initMetrics() {
        Tags tags = Tags.of("tenant", String.valueOf(tenantId));
        this.longTaskTimer = LongTaskTimer
                .builder(DURATION_OF_RECOVERY_TASK)
                .description("duration of recovery task").tags(tags)
                .register(meterRegistry);
        Gauge.builder(NUMBER_OF_ELEMENTS_RECOVERED_LAST_RECOVERY, numberOfElementsRecoveredDuringTheLastRecover,
                AtomicLong::doubleValue)
                .description("number of elements recovered").baseUnit("elements").tags(tags)
                .register(meterRegistry);
        numberOfElementsRecoveredTotal = Counter.builder(NUMBER_OF_ELEMENTS_RECOVERED_TOTAL)
                .baseUnit("elements").description("Total number of elements recovered").tags(tags)
                .register(meterRegistry);
        numberOfRecoverExecuted = Counter.builder(NUMBER_OF_RECOVERY)
                .baseUnit("executions").description("Number of recovery executed").tags(tags)
                .register(meterRegistry);
    }

    @Value("${bonita.tenant.recover.read_batch_size:5000}")
    public void setReadBatchSize(int readBatchSize) {
        this.readBatchSize = readBatchSize;
    }

    @Value("${bonita.tenant.recover.consider_elements_older_than:PT1H}")
    public void setConsiderElementsOlderThan(String considerElementsOlderThan) {
        setConsiderElementsOlderThan(Duration.parse(considerElementsOlderThan));
    }

    @Value("${bonita.tenant.work.batch_restart_size:1000}")
    public void setBatchRestartSize(int batchRestartSize) {
        this.batchRestartSize = batchRestartSize;
    }

    @Value("${tenantId}")
    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
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
        List<ElementToRecover> elementsToRecover = new ArrayList<>();
        try {
            elementsToRecover.addAll(getAllElementsToRecover(PROCESS,
                    (q) -> processInstanceService.getProcessInstanceIdsToRecover(considerElementsOlderThan, q)));
            elementsToRecover.addAll(getAllElementsToRecover(FLOWNODE,
                    (q) -> flowNodeInstanceService.getFlowNodeInstanceIdsToRecover(considerElementsOlderThan, q)));
            elementsToRecover.addAll(getAllElementsToRecover(FLOWNODE,
                    (q) -> flowNodeInstanceService.getGatewayInstanceIdsToRecover(considerElementsOlderThan, q)));
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
        RecoveryMonitor recoveryMonitor = recoveryMonitorProvider.getObject();
        recoveryMonitor.startNow(elementsToRecover.size());
        executeInBatch(recoveryMonitor, elementsToRecover.stream()
                .filter(e1 -> e1.getType() == FLOWNODE)
                .collect(Collectors.toList()), ids -> flowNodesRecover.execute(recoveryMonitor, ids));
        executeInBatch(recoveryMonitor, elementsToRecover.stream()
                .filter(e -> e.getType() == PROCESS)
                .collect(Collectors.toList()), ids -> processesRecover.execute(recoveryMonitor, ids));

        recoveryMonitor.printSummary();
        long numberOfElementRecovered = recoveryMonitor.getNumberOfElementRecovered();
        numberOfElementsRecoveredTotal.increment(numberOfElementRecovered);
        numberOfElementsRecoveredDuringTheLastRecover.set(numberOfElementRecovered);
        numberOfRecoverExecuted.increment();
    }

    protected void executeInBatch(RecoveryMonitor recoveryMonitor, List<ElementToRecover> elements,
            BatchExecution execution) {
        for (List<ElementToRecover> batchElementsIds : split(elements, batchRestartSize)) {
            try {
                userTransactionService.executeInTransaction(() -> {
                    execution.execute(
                            batchElementsIds.stream().map(ElementToRecover::getId).collect(Collectors.toList()));
                    return null;
                });
            } catch (Exception e) {
                log.warn(
                        "Error processing batch of elements to recover, they will be recovered next time: {}, Cause: {}: {}",
                        batchElementsIds, e.getClass().getName(), e.getMessage());
                log.debug("Cause", e);
            }
            if (batchElementsIds.size() == batchRestartSize) {
                // only print progress when there is more than one page
                recoveryMonitor.printProgress();
            }
        }
    }

    /**
     * Recover all elements considered as "stuck".
     * Only recover elements older than a duration configured with {@link #setConsiderElementsOlderThan(String)}.
     */
    public void recoverAllElements() {
        longTaskTimer.record(() -> {
            try {
                sessionAccessor.setTenantId(tenantId);
                List<ElementToRecover> allElementsToRecover = userTransactionService.executeInTransaction(
                        () -> RecoveryService.this.getAllElementsToRecover(considerElementsOlderThan));
                log.debug("Found {} that can potentially be recovered", allElementsToRecover.size());
                recover(allElementsToRecover);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<ElementToRecover> getAllElementsToRecover(ElementToRecover.Type type, IdsRetriever idsRetriever)
            throws SBonitaException {
        // using a too low page size (100) causes too many access to the database and causes timeout exception if there are lot of elements.
        // As we retrieve only the id we can use a greater page size
        QueryOptions queryOptions = new QueryOptions(0, readBatchSize);
        final List<Long> ids = new ArrayList<>();
        List<Long> elementsIds;
        log.debug("Start detecting {} to recover...", type);
        do {
            elementsIds = idsRetriever.getIds(queryOptions);
            queryOptions = QueryOptions.getNextPage(queryOptions);
            ids.addAll(elementsIds);
        } while (elementsIds.size() == queryOptions.getNumberOfResults());
        log.debug("Found {} {} to recover", elementsIds.size(), type);
        return ids
                .stream().map(id -> ElementToRecover.builder().id(id).type(type).build())
                .collect(Collectors.toList());
    }

    private interface BatchExecution {

        void execute(List<Long> ids) throws Exception;
    }

    private interface IdsRetriever {

        List<Long> getIds(QueryOptions queryOptions) throws SBonitaException;
    }
}
