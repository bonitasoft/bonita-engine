/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data;

import static org.bonitasoft.engine.business.data.DataRetentionSettings.DATA_RETENTION_ENABLED_PROPERTY;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.springframework.stereotype.Component;

/**
 * Records in the queriable log each change of the data retention state (enabled / disabled by the
 * {@value DataRetentionSettings#DATA_RETENTION_ENABLED_PROPERTY} property), so that the periods
 * during which BDM changes were not tracked can be identified later, including after an upgrade to a
 * Subscription edition.
 * <p>
 * The state is checked at each platform start: an entry is written when no entry exists yet, so that the history
 * always shows the initial state, and then only when the state differs from the last recorded one.
 * Nodes of a cluster are not synchronized for this check: nodes starting at the same time can each write the same
 * entry. The duplicate is harmless, as the history is informative only.
 * <p>
 * A failure to read the last recorded state is only logged. The entry itself is written when the tenant services
 * initialization transaction commits, like any queriable log entry.
 */
@Slf4j
@Component
public class DataRetentionStateRecorder implements TenantLifecycleService {

    public static final String ACTION_TYPE = SDataRetentionStateLogBuilder.ACTION_TYPE_PREFIX + "_"
            + ActionType.UPDATED.name();
    public static final String STATE_ENABLED = "ENABLED";
    public static final String STATE_DISABLED = "DISABLED";

    private final DataRetentionSettings dataRetentionSettings;
    private final QueriableLoggerService queriableLoggerService;

    public DataRetentionStateRecorder(DataRetentionSettings dataRetentionSettings,
            QueriableLoggerService queriableLoggerService) {
        this.dataRetentionSettings = dataRetentionSettings;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public void init() {
        boolean enabled = dataRetentionSettings.isEnabled();
        if (!enabled) {
            log.warn("Data retention is disabled ({}=false): BDM changes are not tracked and the retention cleanup "
                    + "job is not scheduled. If retention is re-enabled later, business data created while disabled "
                    + "will not be eligible for deletion, and data modified while disabled keeps its previous "
                    + "last-modification date. See "
                    + "https://documentation.ofelia.com/bonita/latest/runtime/bdm-data-retention#legacy-data "
                    + "before re-enabling.", DATA_RETENTION_ENABLED_PROPERTY);
        }
        // An audit entry is not worth a platform that refuses to start: read failures are only logged.
        try {
            recordStateIfChanged(enabled);
        } catch (Exception e) {
            log.warn("Unable to record the data retention state in the queriable log: {} - {}",
                    e.getClass().getName(), e.getMessage());
            log.debug("Caused by", e);
        }
    }

    private void recordStateIfChanged(boolean enabled) throws SBonitaReadException {
        String currentState = enabled ? STATE_ENABLED : STATE_DISABLED;
        Optional<String> lastState = getLastRecordedState();
        if (lastState.filter(currentState::equals).isPresent()) {
            return;
        }
        var logBuilder = new SDataRetentionStateLogBuilder();
        logBuilder.setActionType(ActionType.UPDATED)
                .actionStatus(SQueriableLog.STATUS_OK)
                .severity(SQueriableLogSeverity.INTERNAL)
                .rawMessage("Data retention " + (enabled ? "enabled" : "disabled") + " by the engine configuration ("
                        + DATA_RETENTION_ENABLED_PROPERTY + "=" + enabled + ")")
                .actionScope(currentState);
        queriableLoggerService.log(this.getClass().getName(), "init", logBuilder.build());
        log.info("Data retention state queued for the queriable log: {}", currentState);
    }

    private Optional<String> getLastRecordedState() throws SBonitaReadException {
        var queryOptions = new QueryOptions(0, 1,
                // timestamp first: on a cluster, each node reserves its own block of ids
                List.of(new OrderByOption(SQueriableLog.class, SQueriableLog.TIME_STAMP, OrderByType.DESC),
                        new OrderByOption(SQueriableLog.class, "id", OrderByType.DESC)),
                List.of(new FilterOption(SQueriableLog.class, SQueriableLog.ACTION_TYPE, ACTION_TYPE)),
                null);
        return queriableLoggerService.searchLogs(queryOptions).stream()
                .findFirst()
                .map(SQueriableLog::getActionScope);
    }
}
