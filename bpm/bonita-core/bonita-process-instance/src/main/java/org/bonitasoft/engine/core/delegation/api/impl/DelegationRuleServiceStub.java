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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.delegation.api.DelegationRuleService;
import org.bonitasoft.engine.core.delegation.api.SDelegationRuleNotFoundException;
import org.bonitasoft.engine.core.delegation.model.SDelegatedHumanTask;
import org.bonitasoft.engine.core.delegation.model.SDelegationRule;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * Placeholder implementation of {@link DelegationRuleService}. Every method throws
 * {@link UnsupportedOperationException}; the real persistence and query behaviour is
 * provided in follow-up PRs.
 * <p>
 * This stub exists so the public {@code DelegationAPI} can be wired through the Spring
 * context end-to-end without depending on the persistence layer being in place.
 */
public class DelegationRuleServiceStub implements DelegationRuleService {

    private static final String MESSAGE = "DelegationRuleService not yet implemented";

    @Override
    public SDelegationRule createOrUpdateRule(final SDelegationRule rule, final List<String> processes)
            throws SBonitaException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public SDelegationRule updateRule(final long ruleId, final EntityUpdateDescriptor descriptor,
            final List<String> newProcesses) throws SBonitaException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void deleteRule(final long ruleId) throws SBonitaException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public SDelegationRule getRule(final long ruleId)
            throws SDelegationRuleNotFoundException, SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public Optional<SDelegationRule> getRuleForDelegator(final long delegatorId) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public long getNumberOfRules(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public List<SDelegationRule> searchRules(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public long getNumberOfDelegatedTasks(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public List<SDelegatedHumanTask> searchDelegatedTasks(final QueryOptions options) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public List<String> getProcessNamesByRuleId(final long ruleId) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public Map<Long, List<String>> getProcessNamesByRuleIds(final List<Long> ruleIds) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public boolean isActiveDelegate(final long delegateId, final long taskId) throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public List<SDelegationRule> getActiveDelegationRulesForDelegate(final long delegateId)
            throws SBonitaReadException {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
