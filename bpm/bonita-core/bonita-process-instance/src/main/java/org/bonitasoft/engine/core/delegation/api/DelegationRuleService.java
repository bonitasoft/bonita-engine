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
package org.bonitasoft.engine.core.delegation.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.delegation.model.SDelegatedHumanTask;
import org.bonitasoft.engine.core.delegation.model.SDelegationRule;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * Engine service managing delegation rule persistence and queries.
 * <p>
 * Concrete CRUD and query behaviour are provided in follow-up PRs. This commit ships a
 * stub implementation that throws {@link UnsupportedOperationException} on every call
 * so the public {@code DelegationAPI} can be wired through the Spring context without
 * the real persistence layer being in place yet.
 * <p>
 * The method shapes here form the contract between the public Java API and the
 * persistence/query layer. Changes to this interface require coordination with whoever
 * owns the persistence/query implementations.
 */
public interface DelegationRuleService {

    /**
     * Creates the rule for the delegator carried by {@code rule.delegatorId}, or replaces it
     * if one already exists. The associated process whitelist is replaced wholesale by the
     * provided list (an empty list is rejected by the API layer before reaching the service).
     * <p>
     * The caller is responsible for populating {@code rule.lastUpdatedBy}
     * and {@code rule.lastUpdatedAt} on the incoming entity. The service does not stamp them —
     * leaving them unset would persist zero values and silently break the audit trail.
     *
     * @param rule the rule fields to persist (id is ignored on create, used on replace;
     *        {@code lastUpdatedBy} and {@code lastUpdatedAt} must be set by the caller)
     * @param processes the process whitelist for this rule (at least one entry)
     * @return the persisted rule with server-assigned fields populated
     * @throws SDelegationRuleCreationException if a cross-field invariant is violated
     *         ({@code delegate == delegator} or {@code startDate >= endDate})
     */
    SDelegationRule createOrUpdateRule(SDelegationRule rule, List<String> processes) throws SBonitaException;

    /**
     * Applies a partial update to an existing rule, optionally replacing its process whitelist
     * in the same persistence call.
     * <p>
     * When {@code newProcesses} is non-null the rule's process whitelist is replaced wholesale;
     * when null, the whitelist is left untouched. Folding both writes into one call mirrors
     * Bonita's multi-entity patterns (cf. {@code IdentityService.createUser(SUser, SContactInfo,
     * SContactInfo, ...)}) and keeps the rule fields and whitelist atomic at the service-contract
     * level instead of relying on the ambient JTA transaction wrapping two separate calls.
     *
     * @param ruleId the identifier of the rule to update
     * @param descriptor the fields to modify (keys correspond to {@link SDelegationRule} attribute names)
     * @param newProcesses the replacement process whitelist, or {@code null} to leave it untouched
     * @return the updated rule
     * @throws SDelegationRuleNotFoundException if no rule with that id exists
     * @throws SDelegationRuleUpdateException if a state-dependent invariant rejects the update
     */
    SDelegationRule updateRule(long ruleId, EntityUpdateDescriptor descriptor, List<String> newProcesses)
            throws SBonitaException;

    /**
     * Deletes the rule with the given id, cascading to its process whitelist entries.
     *
     * @throws SDelegationRuleNotFoundException if no rule with that id exists
     */
    void deleteRule(long ruleId) throws SBonitaException;

    /**
     * Loads a single rule by id.
     *
     * @throws SDelegationRuleNotFoundException if no rule with that id exists
     */
    SDelegationRule getRule(long ruleId) throws SDelegationRuleNotFoundException, SBonitaReadException;

    /**
     * Returns the single delegation rule registered for the given delegator, if any.
     * <p>
     * Each user may hold at most one rule at a time (uniqueness on {@code delegatorId} is
     * enforced by the persistence layer). Used by the API layer to detect the upsert path
     * and by the permission cache to seed per-delegator state.
     *
     * @param delegatorId the Id of the user being delegated
     * @return the rule for this delegator, or {@link Optional#empty()} if none is registered
     */
    Optional<SDelegationRule> getRuleForDelegator(long delegatorId) throws SBonitaReadException;

    /** Returns the number of rules matching the given query options. */
    long getNumberOfRules(QueryOptions options) throws SBonitaReadException;

    /** Returns the rules matching the given query options. */
    List<SDelegationRule> searchRules(QueryOptions options) throws SBonitaReadException;

    /** Returns the number of delegated tasks matching the given query options. */
    long getNumberOfDelegatedTasks(QueryOptions options) throws SBonitaReadException;

    /** Returns the delegated tasks matching the given query options. */
    List<SDelegatedHumanTask> searchDelegatedTasks(QueryOptions options) throws SBonitaReadException;

    /** Returns the process whitelist for a single rule (process names in insertion order). */
    List<String> getProcessNamesByRuleId(long ruleId) throws SBonitaReadException;

    /**
     * Batch-fetches process whitelists for multiple rules in a single query, indexed by rule id.
     * Empty list values are returned for rules with no whitelist entries.
     */
    Map<Long, List<String>> getProcessNamesByRuleIds(List<Long> ruleIds) throws SBonitaReadException;

    /**
     * Returns {@code true} if the requesting user is an active delegate authorised to act on
     * the given task. Core check used by the {@code isInvolved} permission rules and
     * {@code TaskExecutionPermissionRule}.
     * <p>
     * Resolves in a single round-trip:
     * <ol>
     * <li>an active delegation rule exists for {@code delegateId} (the rule's
     * {@code [startDate, endDate]} window contains "now");</li>
     * <li>the task's assignee equals the rule's {@code delegatorId};</li>
     * <li>the task's root process (resolved via {@code rootContainerId}) is named in the
     * rule's process whitelist.</li>
     * </ol>
     * Fast-exit if no active rule exists for the user — the permission path for
     * non-delegate callers must not pay the cost of the full check.
     *
     * @param delegateId the Id of the user requesting access
     * @param taskId the human task the caller wants to act on
     * @return {@code true} if the caller is an active delegate for this task
     */
    boolean isActiveDelegate(long delegateId, long taskId) throws SBonitaReadException;

    /**
     * Returns every active rule (today within {@code [startDate, endDate]}) where the given
     * user is the delegate, each carrying its process whitelist. Used by the permission cache
     * to seed per-delegate state.
     *
     * @param delegateId the Id of the user receiving access
     * @return the active rules where {@code delegateId} is the delegate (may be empty)
     */
    List<SDelegationRule> getActiveDelegationRulesForDelegate(long delegateId) throws SBonitaReadException;

    /**
     * Returns {@code true} if the requesting user is an active delegate authorised to act on
     * any human task currently belonging to the given root process instance. Counterpart of
     * {@link #isActiveDelegate(long, long)} for the case-scoped permission checks, where the
     * resource id is a process-instance id rather than a task id.
     * <p>
     * Resolves in a single round-trip:
     * <ol>
     * <li>an active delegation rule exists for {@code delegateId} (the rule's
     * {@code [startDate, endDate]} window contains "now");</li>
     * <li>at least one human task in {@code processInstanceId} (matched via the task's
     * {@code logicalGroup2}, the canonical root-process-instance-id field) has an
     * assignee that equals the rule's {@code delegatorId};</li>
     * <li>the process whose instance is {@code processInstanceId} is named in the rule's
     * process whitelist.</li>
     * </ol>
     * Fast-exit if no active rule exists for the user - the permission path for non-delegate
     * callers must not pay the cost of the full check.
     *
     * @param delegateId the Id of the user requesting access
     * @param processInstanceId the root process instance the caller wants to act on
     * @return {@code true} if the caller is an active delegate for at least one task in this process instance
     */
    boolean isActiveDelegateForProcessInstance(long delegateId, long processInstanceId) throws SBonitaReadException;
}
