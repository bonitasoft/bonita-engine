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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Status;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
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
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
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

    /**
     * Cache region holding, per delegate user id, the list of that user's currently-active
     * delegation rules. Used to short-circuit the permission hot-path existence check
     * (see {@link #isActiveDelegate(long, long)}). Registered as a local-only cache in cluster
     * mode (see {@code EngineClusterConfiguration}) so the hot read path stays in-JVM.
     */
    public static final String ACTIVE_DELEGATION_RULES_CACHE = "active_delegation_rules";

    public static final String STATUS_SCHEDULED = "scheduled";

    public static final String STATUS_ACTIVE = "active";

    public static final String STATUS_EXPIRED = "expired";

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final CacheService cacheService;

    private final UserTransactionService userTransactionService;

    private final BroadcastService broadcastService;

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
        final Set<Long> affectedDelegates = new HashSet<>();
        final SDelegationRule persisted;
        if (existing.isEmpty()) {
            recorder.recordInsert(new InsertRecord(rule), RECORD_TYPE_DELEGATION_RULE);
            persisted = rule;
            log.debug("Inserted delegation rule <{}> for delegator <{}>", persisted.getId(), rule.getDelegatorId());
        } else {
            final SDelegationRule existingRule = existing.get();
            // Capture the previous delegate before recordUpdate mutates the row in-memory: re-delegating
            // to a different user must invalidate both the old and the new delegate's cache entries.
            affectedDelegates.add(existingRule.getDelegateId());
            // recordUpdate applies the descriptor in-memory via ClassReflector.setField, so
            // existingRule now carries the new field values and can be returned as the persisted view.
            recorder.recordUpdate(UpdateRecord.buildSetFields(existingRule, buildUpsertDescriptor(rule)),
                    RECORD_TYPE_DELEGATION_RULE);
            deleteRuleProcessesByRuleId(existingRule.getId());
            persisted = existingRule;
            log.debug("Replaced delegation rule <{}> for delegator <{}>", persisted.getId(), rule.getDelegatorId());
        }
        insertRuleProcesses(persisted.getId(), processes);
        affectedDelegates.add(rule.getDelegateId());
        scheduleCacheEviction(affectedDelegates);
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
        // Capture the previous delegate before recordUpdate mutates the row in-memory.
        final long previousDelegateId = existing.getDelegateId();
        // recordUpdate applies the descriptor in-memory via ClassReflector.setField, so
        // existing now carries the new field values and can be returned as the persisted view.
        recorder.recordUpdate(UpdateRecord.buildSetFields(existing, descriptor), RECORD_TYPE_DELEGATION_RULE);
        if (newProcesses != null) {
            deleteRuleProcessesByRuleId(existing.getId());
            insertRuleProcesses(existing.getId(), newProcesses);
        }
        // Invalidate the previous delegate and, if the descriptor reassigned the delegate, the new one too
        // (the set dedups when the delegate is unchanged).
        final Set<Long> affectedDelegates = new HashSet<>();
        affectedDelegates.add(previousDelegateId);
        affectedDelegates.add(existing.getDelegateId());
        scheduleCacheEviction(affectedDelegates);
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
        scheduleCacheEviction(Set.of(rule.getDelegateId()));
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
     * <li>A cached fast-exit on the caller's active rule set (see
     * {@link #getActiveDelegationRulesForDelegate(long)}) — returns {@code false} immediately,
     * without touching the DB, when the caller is not a delegate of any currently-active rule,
     * sparing the multi-join cost on every form-display authorization check for users who aren't
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
        // Stage 1: cached fast-exit. Non-delegates (the overwhelming majority of callers) return here
        // without touching the DB once their empty rule set is cached.
        if (getActiveDelegationRulesForDelegate(delegateId).isEmpty()) {
            return false;
        }
        // Stage 2: the task-specific match stays a DB query and remains the source of truth, so a
        // cached-but-now-expired rule, or one that no longer matches the task, still resolves correctly.
        final Long count = persistenceService.selectOne(new SelectOneDescriptor<>(QUERY_IS_ACTIVE_DELEGATE_FOR_TASK,
                Map.of("delegateId", delegateId, "taskId", taskId, "now", currentTimeMillis()),
                SDelegationRule.class, Long.class));
        return count != null && count > 0L;
    }

    @Override
    public List<SDelegationRule> getActiveDelegationRulesForDelegate(final long delegateId)
            throws SBonitaReadException {
        // Permission hot path: the portal fires a burst of REST calls per task-form display, each
        // re-checking involvement. Serve the per-delegate active-rule set from an in-memory cache to
        // avoid a DB round-trip on every call. The empty list is cached too, so repeated checks for
        // a non-delegate (the common case) also cost zero DB. The cache is invalidated on every rule
        // write (see createOrUpdateRule/updateRule/deleteRule); a future-dated rule that activates
        // while a stale entry is warm is only reflected after the region TTL elapses.
        final List<SDelegationRule> cached = getCachedActiveRules(delegateId);
        if (cached != null) {
            return cached;
        }
        // Copy before caching so the shared cached instance cannot be mutated by a caller: the
        // same reference is handed to every reader of this delegate's entry. List.copyOf gives an
        // immutable defensive copy, so it stays safe even if queryActiveRulesForDelegate is later
        // refactored to reuse or share the underlying list.
        final List<SDelegationRule> rules = List.copyOf(queryActiveRulesForDelegate(delegateId));
        putCachedActiveRules(delegateId, rules);
        return rules;
    }

    private List<SDelegationRule> queryActiveRulesForDelegate(final long delegateId) throws SBonitaReadException {
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

    @SuppressWarnings("unchecked")
    private List<SDelegationRule> getCachedActiveRules(final long delegateId) {
        try {
            return (List<SDelegationRule>) cacheService.get(ACTIVE_DELEGATION_RULES_CACHE, delegateId);
        } catch (final SCacheException e) {
            // Never fail a permission check because of a cache hiccup: fall back to the DB.
            log.debug("Could not read delegation-rule cache for delegate <{}>, falling back to DB", delegateId, e);
            return null;
        }
    }

    private void putCachedActiveRules(final long delegateId, final List<SDelegationRule> rules) {
        try {
            cacheService.store(ACTIVE_DELEGATION_RULES_CACHE, delegateId, rules);
        } catch (final SCacheException e) {
            // A persistent store failure means every read falls back to the DB: surface it so the
            // cache-tier degradation does not go unnoticed in production.
            log.warn("Could not store delegation-rule cache for delegate <{}>", delegateId, e);
        }
    }

    /**
     * Evicts the given delegates' cached active-rule sets once the current transaction commits.
     * <p>
     * Eviction is deferred to after-commit on purpose: doing it inline (pre-commit) is racy even on a
     * single node, because a concurrent read in another transaction could observe the not-yet-committed
     * state and re-cache a stale set that then survives until the region TTL. After commit we evict the
     * local node directly and broadcast an {@link EvictActiveDelegationRulesCacheTask} to the other
     * cluster nodes (whose caches are node-local), so no node keeps serving a stale set. The broadcast
     * is a no-op in single-node deployments. The after-commit + cluster-broadcast invalidation shape
     * follows {@code ClassLoaderServiceImpl}, with one deliberate difference: that service waits on
     * the broadcast and propagates per-node errors, whereas this one fire-and-forgets (see
     * {@link #broadcastRemoteEviction(Set)}), since the region TTL bounds any missed eviction.
     * <p>
     * After-commit eviction does not make the cache strictly coherent: a reader that loaded the
     * active-rule set <em>before</em> a concurrent write can still re-cache its now-stale result
     * <em>after</em> this eviction fires (its cache-put in {@link #getActiveDelegationRulesForDelegate(long)}
     * races behind the eviction), and that entry then lives until the region TTL elapses. The cache is
     * therefore eventually consistent, bounded by {@code timeToLiveSeconds}; this residual staleness is
     * accepted. The permission decision stays correct regardless, because the task/case match is always
     * re-checked against the DB (see {@link #isActiveDelegate(long, long)} stage 2).
     */
    private void scheduleCacheEviction(final Set<Long> delegateIds) {
        final Set<Long> ids = new HashSet<>(delegateIds);
        try {
            userTransactionService.registerBonitaSynchronization((BonitaTransactionSynchronization) txState -> {
                if (txState == Status.STATUS_COMMITTED) {
                    ids.forEach(this::evictFromLocalCache);
                    broadcastRemoteEviction(ids);
                }
            });
        } catch (final STransactionNotFoundException e) {
            // Writes always run inside a transaction; if somehow none is active, evict the local node
            // now as a best effort and let the other nodes refresh on TTL expiry.
            ids.forEach(this::evictFromLocalCache);
            log.debug("No active transaction; evicted delegation cache locally only for {}", ids, e);
        }
    }

    private void evictFromLocalCache(final long delegateId) {
        try {
            cacheService.remove(ACTIVE_DELEGATION_RULES_CACHE, delegateId);
        } catch (final SCacheException e) {
            // A failed eviction leaves a stale active-rule set served until the region TTL elapses:
            // surface it so the cache-tier degradation does not go unnoticed in production.
            log.warn("Could not invalidate delegation-rule cache for delegate <{}>", delegateId, e);
        }
    }

    private void broadcastRemoteEviction(final Set<Long> delegateIds) {
        // Unlike ClassLoaderServiceImpl, which waits on the broadcast (executeOnOthersAndWait) and
        // propagates per-node errors, this path deliberately fire-and-forgets: it runs in
        // afterCompletion (the transaction has already committed), where blocking or throwing must
        // not escape the completion callback. A missed broadcast just leaves the other nodes to
        // refresh on TTL expiry. The submission failure is logged at warn so the cache-tier
        // degradation is still visible to operators.
        // The Future returned by executeOnOthers is deliberately ignored: each target node logs its
        // own per-id eviction failures (see EvictActiveDelegationRulesCacheTask), so a remote
        // SCacheException does not surface here, and a missed eviction falls back to TTL.
        try {
            broadcastService.executeOnOthers(new EvictActiveDelegationRulesCacheTask(delegateIds));
        } catch (final RuntimeException e) {
            log.warn("Could not broadcast delegation-cache eviction to other nodes for {}", delegateIds, e);
        }
    }

    /**
     * Case-scoped counterpart of {@link #isActiveDelegate(long, long)} with the same two-stage
     * fast-exit: the cached active-rule set (see {@link #getActiveDelegationRulesForDelegate(long)})
     * spares non-delegate callers the multi-join cost on every case-resource permission check, and
     * the full {@code isActiveDelegateForProcessInstance} named query enforces the active-window,
     * whitelist match, root-process resolution (via {@code logicalGroup2}) and per-task
     * assignee-equals-delegator predicate DB-side.
     */
    @Override
    public boolean isActiveDelegateForProcessInstance(final long delegateId, final long processInstanceId)
            throws SBonitaReadException {
        // Stage 1: cached fast-exit (see isActiveDelegate for the rationale).
        if (getActiveDelegationRulesForDelegate(delegateId).isEmpty()) {
            return false;
        }
        // Stage 2: the case-specific match stays a DB query and remains the source of truth.
        final Long count = persistenceService.selectOne(new SelectOneDescriptor<>(
                QUERY_IS_ACTIVE_DELEGATE_FOR_PROCESS_INSTANCE,
                Map.of("delegateId", delegateId, "processInstanceId", processInstanceId, "now", currentTimeMillis()),
                SDelegationRule.class, Long.class));
        return count != null && count > 0L;
    }
}
