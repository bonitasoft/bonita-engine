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

import java.util.Collections;
import java.util.List;
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
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
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
 * The write-side methods ({@code createOrUpdateRule}, {@code updateRule}, {@code deleteRule})
 * and the two read helpers they depend on ({@code getRule}, {@code getRuleForDelegator}) are
 * implemented here. The remaining query methods — {@code searchRules}, {@code getNumberOfRules},
 * {@code searchDelegatedTasks}, {@code getNumberOfDelegatedTasks},
 * {@code getProcessNamesByRuleId}, {@code getProcessNamesByRuleIds},
 * {@code isActiveDelegate} and {@code getActiveDelegationRulesForDelegate} — currently throw
 * {@link UnsupportedOperationException} pending a follow-up.
 */
@Slf4j
@Service("delegationRuleService")
@RequiredArgsConstructor
public class DelegationRuleServiceImpl implements DelegationRuleService {

    static final String RECORD_TYPE_DELEGATION_RULE = "DELEGATION_RULE";

    static final String RECORD_TYPE_DELEGATION_RULE_PROCESS = "DELEGATION_RULE_PROCESS";

    static final String QUERY_RULE_BY_DELEGATOR_ID = "getDelegationRuleByDelegatorId";

    private static final String QUERY_PENDING_MESSAGE = "DelegationRuleService query methods not yet implemented";

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
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public List<SDelegationRule> searchRules(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public long getNumberOfDelegatedTasks(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public List<SDelegatedHumanTask> searchDelegatedTasks(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public List<String> getProcessNamesByRuleId(final long ruleId) throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public Map<Long, List<String>> getProcessNamesByRuleIds(final List<Long> ruleIds) throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public boolean isActiveDelegate(final long delegateId, final long taskId) throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }

    @Override
    public List<SDelegationRule> getActiveDelegationRulesForDelegate(final long delegateId)
            throws SBonitaReadException {
        throw new UnsupportedOperationException(QUERY_PENDING_MESSAGE);
    }
}
