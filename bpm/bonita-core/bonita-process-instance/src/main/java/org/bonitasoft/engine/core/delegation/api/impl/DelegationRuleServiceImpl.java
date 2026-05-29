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
package org.bonitasoft.engine.core.delegation.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.delegation.api.DelegationRuleService;
import org.bonitasoft.engine.core.delegation.api.SDelegationRuleCreationException;
import org.bonitasoft.engine.core.delegation.api.SDelegationRuleNotFoundException;
import org.bonitasoft.engine.core.delegation.api.SDelegationRuleUpdateException;
import org.bonitasoft.engine.core.delegation.model.SDelegatedHumanTask;
import org.bonitasoft.engine.core.delegation.model.SDelegationRule;
import org.bonitasoft.engine.core.delegation.model.SDelegationRuleProcess;
import org.bonitasoft.engine.delegation.DelegationRuleFilterKeys;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.springframework.stereotype.Service;

/**
 * Persistence-backed implementation of {@link DelegationRuleService}.
 * <p>
 * Date-window enforcement (active rules / active delegate / delegated tasks) is pushed
 * DB-side via {@code :now} parameters on the HQL queries — see {@link #currentTimeMillis()}
 * for the test seam. Root-process resolution uses the task's {@code logicalGroup2} column
 * (the canonical root-process-instance-id field) rather than the immediate parent, so
 * subprocess tasks resolve to the top-level process the rule's whitelist matched against.
 */
@Slf4j
@Service("delegationRuleService")
@RequiredArgsConstructor
public class DelegationRuleServiceImpl implements DelegationRuleService {

    static final String RECORD_TYPE_DELEGATION_RULE = "DELEGATION_RULE";

    static final String RECORD_TYPE_DELEGATION_RULE_PROCESS = "DELEGATION_RULE_PROCESS";

    static final String QUERY_RULE_BY_DELEGATOR_ID = "getDelegationRuleByDelegatorId";

    static final String QUERY_PROCESS_NAMES_BY_RULE_ID = "getProcessNamesByDelegationRuleId";

    static final String QUERY_PROCESS_NAMES_BY_RULE_IDS = "getProcessNamesByDelegationRuleIds";

    static final String QUERY_ACTIVE_RULES_FOR_DELEGATE = "getActiveDelegationRulesForDelegate";

    static final String QUERY_IS_ACTIVE_DELEGATE_FOR_TASK = "isActiveDelegateForTask";

    static final String QUERY_IS_ACTIVE_DELEGATE_FOR_PROCESS_INSTANCE = "isActiveDelegateForProcessInstance";

    static final String QUERY_EXISTS_ACTIVE_RULE_FOR_DELEGATE = "existsActiveRuleForDelegate";

    public static final String STATUS_SCHEDULED = "scheduled";

    public static final String STATUS_ACTIVE = "active";

    public static final String STATUS_EXPIRED = "expired";

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    @Override
    public SDelegationRule createOrUpdateRule(final SDelegationRule rule, final List<String> processes)
            throws SBonitaException {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("processes must contain at least one entry");
        }
        if (rule.getDelegateId() == rule.getDelegatorId()) {
            throw new SDelegationRuleCreationException(
                    "delegator and delegate must be different users (got id=" + rule.getDelegatorId() + ")");
        }
        if (rule.getStartDate() >= rule.getEndDate()) {
            throw new SDelegationRuleCreationException(
                    "startDate must be strictly before endDate (got startDate=" + rule.getStartDate()
                            + ", endDate=" + rule.getEndDate() + ")");
        }
        // Check-then-insert is non-atomic; the UNIQUE(delegator_id) constraint enforces correctness
        // under concurrent inserts (loser's insert fails and bubbles up as SRecorderException).
        final Optional<SDelegationRule> existing = getRuleForDelegator(rule.getDelegatorId());
        final SDelegationRule persisted;
        if (existing.isEmpty()) {
            recorder.recordInsert(new InsertRecord(rule), RECORD_TYPE_DELEGATION_RULE);
            persisted = rule;
            log.debug("Inserted delegation rule <{}> for delegator <{}>", persisted.getId(), rule.getDelegatorId());
        } else {
            final SDelegationRule existingRule = existing.get();
            // recordUpdate applies the descriptor in-memory via ClassReflector.setField, so
            // existingRule now carries the new field values and can be returned as the persisted view.
            recorder.recordUpdate(UpdateRecord.buildSetFields(existingRule, buildUpsertDescriptor(rule)),
                    RECORD_TYPE_DELEGATION_RULE);
            deleteRuleProcessesByRuleId(existingRule.getId());
            persisted = existingRule;
            log.debug("Replaced delegation rule <{}> for delegator <{}>", persisted.getId(), rule.getDelegatorId());
        }
        insertRuleProcesses(persisted.getId(), processes);
        return persisted;
    }

    private EntityUpdateDescriptor buildUpsertDescriptor(final SDelegationRule rule) {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.DELEGATE_ID_KEY, rule.getDelegateId());
        descriptor.addField(SDelegationRule.START_DATE_KEY, rule.getStartDate());
        descriptor.addField(SDelegationRule.END_DATE_KEY, rule.getEndDate());
        descriptor.addField(SDelegationRule.LAST_UPDATED_BY_KEY, rule.getLastUpdatedBy());
        descriptor.addField(SDelegationRule.LAST_UPDATED_AT_KEY, rule.getLastUpdatedAt());
        return descriptor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>State-dependent validation:</b> the update is rejected with
     * {@link SDelegationRuleUpdateException} when, after applying the descriptor:
     * <ul>
     * <li>{@code delegateId} would equal the existing rule's {@code delegatorId}, or</li>
     * <li>the resulting {@code [startDate, endDate]} window would be inverted
     * ({@code startDate >= endDate}) — including the partial case where the descriptor
     * carries only one of the two bounds.</li>
     * </ul>
     * These checks require loading the persisted row and therefore live here rather than in
     * the API layer.
     */
    @Override
    public SDelegationRule updateRule(final long ruleId, final EntityUpdateDescriptor descriptor,
            final List<String> newProcesses) throws SBonitaException {
        final SDelegationRule existing = getRule(ruleId);
        validateWindow(existing, descriptor);
        validateDelegateDistinctFromDelegator(existing, descriptor);
        // recordUpdate applies the descriptor in-memory via ClassReflector.setField, so
        // existing now carries the new field values and can be returned as the persisted view.
        recorder.recordUpdate(UpdateRecord.buildSetFields(existing, descriptor), RECORD_TYPE_DELEGATION_RULE);
        if (newProcesses != null) {
            deleteRuleProcessesByRuleId(existing.getId());
            insertRuleProcesses(existing.getId(), newProcesses);
        }
        log.debug("Updated delegation rule <{}>", ruleId);
        return existing;
    }

    /**
     * Inverted-date guard. Applies the descriptor (with fallback to {@code existing} for any
     * bound the descriptor omits) and rejects the result if {@code startDate >= endDate}. Covers
     * all three cases — start-only, end-only and both-bounds — in one place.
     */
    private void validateWindow(final SDelegationRule existing, final EntityUpdateDescriptor descriptor)
            throws SBonitaException {
        final boolean hasStart = descriptor.getFields().containsKey(SDelegationRule.START_DATE_KEY);
        final boolean hasEnd = descriptor.getFields().containsKey(SDelegationRule.END_DATE_KEY);
        if (!hasStart && !hasEnd) {
            return;
        }
        final long newStart = hasStart ? (Long) descriptor.getFields().get(SDelegationRule.START_DATE_KEY)
                : existing.getStartDate();
        final long newEnd = hasEnd ? (Long) descriptor.getFields().get(SDelegationRule.END_DATE_KEY)
                : existing.getEndDate();
        if (newStart >= newEnd) {
            throw new SDelegationRuleUpdateException("startDate must be strictly before endDate (got startDate="
                    + newStart + ", endDate=" + newEnd + ")");
        }
    }

    /**
     * Cross-field guard: a descriptor that changes {@code delegateId} must not result in a rule
     * where the delegate equals the existing {@code delegatorId}. The API layer cannot perform
     * this check on update because it does not load the persisted row.
     */
    private void validateDelegateDistinctFromDelegator(final SDelegationRule existing,
            final EntityUpdateDescriptor descriptor) throws SBonitaException {
        if (!descriptor.getFields().containsKey(SDelegationRule.DELEGATE_ID_KEY)) {
            return;
        }
        final long newDelegate = (Long) descriptor.getFields().get(SDelegationRule.DELEGATE_ID_KEY);
        if (newDelegate == existing.getDelegatorId()) {
            throw new SDelegationRuleUpdateException("delegate must be different from delegator (got id="
                    + newDelegate + ")");
        }
    }

    @Override
    public void deleteRule(final long ruleId) throws SBonitaException {
        final SDelegationRule rule = persistenceService
                .selectById(new SelectByIdDescriptor<>(SDelegationRule.class, ruleId));
        if (rule == null) {
            throw new SDelegationRuleNotFoundException(ruleId);
        }
        // Whitelist rows are removed by the delegation_rule_process foreign-key ON DELETE CASCADE
        // declared in the DDL — no explicit recordDeleteAll needed here.
        recorder.recordDelete(new DeleteRecord(rule), RECORD_TYPE_DELEGATION_RULE);
        log.debug("Deleted delegation rule <{}>", ruleId);
    }

    @Override
    public SDelegationRule getRule(final long ruleId)
            throws SDelegationRuleNotFoundException, SBonitaReadException {
        final SDelegationRule rule = persistenceService
                .selectById(new SelectByIdDescriptor<>(SDelegationRule.class, ruleId));
        if (rule == null) {
            throw new SDelegationRuleNotFoundException(ruleId);
        }
        return rule;
    }

    @Override
    public Optional<SDelegationRule> getRuleForDelegator(final long delegatorId) throws SBonitaReadException {
        final SelectOneDescriptor<SDelegationRule> descriptor = new SelectOneDescriptor<>(QUERY_RULE_BY_DELEGATOR_ID,
                Collections.singletonMap("delegatorId", delegatorId), SDelegationRule.class);
        return Optional.ofNullable(persistenceService.selectOne(descriptor));
    }

    private void insertRuleProcesses(final long ruleId, final List<String> processes) throws SRecorderException {
        for (final String processName : processes) {
            final SDelegationRuleProcess entry = SDelegationRuleProcess.builder()
                    .delegationRuleId(ruleId)
                    .processName(processName)
                    .build();
            recorder.recordInsert(new InsertRecord(entry), RECORD_TYPE_DELEGATION_RULE_PROCESS);
        }
    }

    private void deleteRuleProcessesByRuleId(final long ruleId) throws SRecorderException {
        recorder.recordDeleteAll(new DeleteAllRecord(SDelegationRuleProcess.class,
                Collections.singletonList(new FilterOption(SDelegationRuleProcess.class,
                        SDelegationRuleProcess.DELEGATION_RULE_ID_KEY, ruleId))));
    }

    @Override
    public long getNumberOfRules(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SDelegationRule.class, rewriteStatusFilter(options), null);
    }

    @Override
    public List<SDelegationRule> searchRules(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SDelegationRule.class, rewriteStatusFilter(options), null);
    }

    /**
     * Package-private seam over {@link System#currentTimeMillis()} so unit tests can pin "now"
     * by stubbing this method via Mockito (e.g. {@code doReturn(FIXED_NOW).when(spy).currentTimeMillis()}
     * on a {@code @Spy} of the service). Production callers always see real wall-clock time.
     */
    // @VisibleForTesting
    long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Translates the virtual {@link DelegationRuleFilterKeys#STATUS STATUS} filter into date-range
     * predicates against {@code startDate}/{@code endDate}. The public-API descriptor forwards
     * {@code STATUS} as the {@link FilterOption} field name; this method strips it before forwarding
     * to Hibernate, which would otherwise reject the unknown column.
     */
    private QueryOptions rewriteStatusFilter(final QueryOptions options) throws SBonitaReadException {
        final List<FilterOption> filters = options.getFilters();
        if (filters == null || filters.isEmpty()) {
            return options;
        }
        final FilterOption statusFilter = findStatusFilter(filters);
        if (statusFilter == null) {
            return options;
        }
        // toString() + lowercase normalises both forms the public API contract accepts:
        // strings (e.g. "active" from REST) and DelegationStatus enum values, whose
        // Enum.toString() returns the uppercase name() form (e.g. "ACTIVE").
        final String statusValue = statusFilter.getValue().toString().toLowerCase(Locale.ROOT);
        final List<FilterOption> rewritten = new ArrayList<>(filters.size() + 1);
        for (final FilterOption filter : filters) {
            if (filter != statusFilter) {
                rewritten.add(filter);
            }
        }
        rewritten.addAll(dateFiltersForStatus(statusValue, currentTimeMillis()));
        return new QueryOptions(options.getFromIndex(), options.getNumberOfResults(),
                options.getOrderByOptions(), rewritten, options.getMultipleFilter());
    }

    /**
     * Locates the single {@link DelegationRuleFilterKeys#STATUS STATUS} filter in {@code filters}
     * and validates that it carries an {@code EQUALS} operation with a non-null value. Returns
     * {@code null} when no STATUS filter is present (the caller short-circuits the rewrite).
     *
     * @throws SBonitaReadException when STATUS appears more than once, when its operation is not
     *         {@code EQUALS}, or when its value is {@code null}
     */
    private FilterOption findStatusFilter(final List<FilterOption> filters) throws SBonitaReadException {
        FilterOption statusFilter = null;
        for (final FilterOption filter : filters) {
            if (DelegationRuleFilterKeys.STATUS.equals(filter.getFieldName())) {
                if (statusFilter != null) {
                    throw new SBonitaReadException("STATUS filter must appear at most once");
                }
                statusFilter = filter;
            }
        }
        if (statusFilter == null) {
            return null;
        }
        if (statusFilter.getFilterOperationType() != FilterOperationType.EQUALS) {
            throw new SBonitaReadException("STATUS filter supports only EQUALS (got "
                    + statusFilter.getFilterOperationType() + ")");
        }
        if (statusFilter.getValue() == null) {
            throw new SBonitaReadException("STATUS filter value must not be null");
        }
        return statusFilter;
    }

    /**
     * Returns the {@link FilterOption} list that replaces the STATUS sentinel for the given
     * lifecycle {@code statusValue}, computed against {@code now}.
     *
     * @throws SBonitaReadException when {@code statusValue} is not one of {@code scheduled},
     *         {@code active}, {@code expired}
     */
    private List<FilterOption> dateFiltersForStatus(final String statusValue, final long now)
            throws SBonitaReadException {
        return switch (statusValue) {
            case STATUS_SCHEDULED -> Collections.singletonList(new FilterOption(SDelegationRule.class,
                    SDelegationRule.START_DATE_KEY, now, FilterOperationType.GREATER));
            case STATUS_ACTIVE -> Arrays.asList(
                    new FilterOption(SDelegationRule.class, SDelegationRule.START_DATE_KEY, now,
                            FilterOperationType.LESS_OR_EQUALS),
                    new FilterOption(SDelegationRule.class, SDelegationRule.END_DATE_KEY, now,
                            FilterOperationType.GREATER_OR_EQUALS));
            case STATUS_EXPIRED -> Collections.singletonList(new FilterOption(SDelegationRule.class,
                    SDelegationRule.END_DATE_KEY, now, FilterOperationType.LESS));
            default -> throw new SBonitaReadException("Unknown STATUS value '%s' (accepted: %s, %s, %s)"
                    .formatted(statusValue, STATUS_SCHEDULED, STATUS_ACTIVE, STATUS_EXPIRED));
        };
    }

    @Override
    public long getNumberOfDelegatedTasks(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SDelegatedHumanTask.class, options,
                Map.of("now", currentTimeMillis()));
    }

    /**
     * Search rows of {@link SDelegatedHumanTask} — the wrapper joining a human task with its
     * delegator's active rule and the root process's deploy info. The HQL named query
     * {@code searchSDelegatedHumanTask} (and its {@code getNumberOf} counterpart) carries the
     * JOIN and date-window predicate; the {@code SearchDelegatedTaskDescriptor} on the
     * subscription side maps the public filter / sort keys onto the participating columns.
     */
    @Override
    public List<SDelegatedHumanTask> searchDelegatedTasks(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SDelegatedHumanTask.class, options,
                Map.of("now", currentTimeMillis()));
    }

    @Override
    public List<String> getProcessNamesByRuleId(final long ruleId) throws SBonitaReadException {
        final SelectListDescriptor<String> descriptor = new SelectListDescriptor<>(QUERY_PROCESS_NAMES_BY_RULE_ID,
                Collections.singletonMap("ruleId", ruleId), SDelegationRuleProcess.class, String.class,
                QueryOptions.ALL_RESULTS);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public Map<Long, List<String>> getProcessNamesByRuleIds(final List<Long> ruleIds) throws SBonitaReadException {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final SelectListDescriptor<Map<String, Object>> descriptor = new SelectListDescriptor<>(
                QUERY_PROCESS_NAMES_BY_RULE_IDS,
                Collections.singletonMap("ruleIds", ruleIds),
                SDelegationRuleProcess.class, QueryOptions.ALL_RESULTS);
        // IN (:ruleIds) is not chunked — Oracle limits IN-lists to 1000 entries.
        // Current callers stay well below that (per-page batch projection at page size ~100).
        final List<Map<String, Object>> rows = persistenceService.selectList(descriptor);
        final Map<Long, List<String>> result = new LinkedHashMap<>();
        for (final Long ruleId : ruleIds) {
            result.put(ruleId, new ArrayList<>());
        }
        for (final Map<String, Object> row : rows) {
            // "ruleId" and "processName" are aliases declared in the
            // "getProcessNamesByDelegationRuleIds" Hibernate named query (new map(... as ...)).
            final Long ruleId = (Long) row.get("ruleId");
            final String processName = (String) row.get("processName");
            // The IN (:ruleIds) filter guarantees ruleId is one of the keys we seeded.
            result.get(ruleId).add(processName);
        }
        return result;
    }

    /**
     * Permission hot-path check executed in two stages:
     * <ol>
     * <li>A cheap fast-exit count on {@code SDelegationRule} alone — returns {@code false}
     * immediately when the caller is not a delegate of any currently-active rule, sparing
     * the multi-join cost on every form-display authorization check for users who aren't
     * standing in for anyone right now.</li>
     * <li>If at least one active rule exists, the full {@code isActiveDelegateForTask}
     * named query joining rule + whitelist + human task + process instance +
     * process-definition deploy info enforces the active-window, whitelist match,
     * task-assignee match, and root-process resolution (via the task's
     * {@code logicalGroup2}, the canonical "root process instance id" field used by
     * the rest of the engine's task-to-root-process queries) DB-side. This method only
     * translates the resulting count into a boolean.</li>
     * </ol>
     */
    @Override
    public boolean isActiveDelegate(final long delegateId, final long taskId) throws SBonitaReadException {
        final long now = currentTimeMillis();
        final Long activeRulesCount = persistenceService.selectOne(new SelectOneDescriptor<>(
                QUERY_EXISTS_ACTIVE_RULE_FOR_DELEGATE,
                Map.of("delegateId", delegateId, "now", now),
                SDelegationRule.class, Long.class));
        if (activeRulesCount == null || activeRulesCount == 0L) {
            return false;
        }
        final Long count = persistenceService.selectOne(new SelectOneDescriptor<>(QUERY_IS_ACTIVE_DELEGATE_FOR_TASK,
                Map.of("delegateId", delegateId, "taskId", taskId, "now", now),
                SDelegationRule.class, Long.class));
        return count != null && count > 0L;
    }

    @Override
    public List<SDelegationRule> getActiveDelegationRulesForDelegate(final long delegateId)
            throws SBonitaReadException {
        // Date-window predicates (startDate <= :now and endDate >= :now) live in the HQL named
        // query, so scheduled/expired rules are filtered DB-side. Result set is bounded only
        // by social structure: one user can be the delegate on N rules (one per delegator who
        // delegated to them — the UNIQUE constraint on delegation_rule is on delegator_id, not
        // delegate_id). In practice this is small, so we accept QueryOptions.ALL_RESULTS and
        // keep the permission-layer hot path call-site free of pagination plumbing.
        return persistenceService.selectList(new SelectListDescriptor<>(QUERY_ACTIVE_RULES_FOR_DELEGATE,
                Map.of("delegateId", delegateId, "now", currentTimeMillis()),
                SDelegationRule.class, QueryOptions.ALL_RESULTS));
    }

    /**
     * Case-scoped counterpart of {@link #isActiveDelegate(long, long)} with the same two-stage
     * fast-exit: the cheap EXISTS pre-check spares non-delegate callers the multi-join cost
     * on every case-resource permission check, and the full {@code isActiveDelegateForProcessInstance}
     * named query enforces the active-window, whitelist match, root-process resolution (via
     * {@code logicalGroup2}) and per-task assignee-equals-delegator predicate DB-side.
     */
    @Override
    public boolean isActiveDelegateForProcessInstance(final long delegateId, final long processInstanceId)
            throws SBonitaReadException {
        final long now = currentTimeMillis();
        final Long activeRulesCount = persistenceService.selectOne(new SelectOneDescriptor<>(
                QUERY_EXISTS_ACTIVE_RULE_FOR_DELEGATE,
                Map.of("delegateId", delegateId, "now", now),
                SDelegationRule.class, Long.class));
        if (activeRulesCount == null || activeRulesCount == 0L) {
            return false;
        }
        final Long count = persistenceService.selectOne(new SelectOneDescriptor<>(
                QUERY_IS_ACTIVE_DELEGATE_FOR_PROCESS_INSTANCE,
                Map.of("delegateId", delegateId, "processInstanceId", processInstanceId, "now", now),
                SDelegationRule.class, Long.class));
        return count != null && count > 0L;
    }
}
