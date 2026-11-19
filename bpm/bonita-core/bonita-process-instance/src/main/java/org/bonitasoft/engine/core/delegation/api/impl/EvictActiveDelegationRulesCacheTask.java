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

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.service.InjectedService;

/**
 * Broadcast task that evicts the given delegates' entries from the node-local
 * {@link DelegationRuleServiceImpl#ACTIVE_DELEGATION_RULES_CACHE} region. It is sent to the other
 * cluster nodes when a delegation-rule write commits, so a node that did not handle the write does
 * not keep serving a stale active-rule set until its region TTL elapses (the writing node evicts its
 * own entry directly). {@code CacheService} is resolved on the target node through
 * {@link InjectedService}. In single-node deployments the broadcast has no other node to run on, so
 * this task is never executed.
 *
 * @author Anthony Birembaut
 */
@Slf4j
public class EvictActiveDelegationRulesCacheTask implements Callable<Void>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Set<Long> delegateIds;

    private transient CacheService cacheService;

    public EvictActiveDelegationRulesCacheTask(final Set<Long> delegateIds) {
        this.delegateIds = delegateIds;
    }

    @Override
    public Void call() {
        for (final Long delegateId : delegateIds) {
            try {
                cacheService.remove(DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE, delegateId);
            } catch (final SCacheException e) {
                // One bad id must not abort the batch: the other delegates still need eviction, and a
                // missed eviction is bounded by the region TTL. Surface it at warn on this node.
                log.warn("Could not evict delegation-rule cache for delegate <{}> on this node", delegateId, e);
            }
        }
        return null;
    }

    @InjectedService
    public void setCacheService(final CacheService cacheService) {
        this.cacheService = cacheService;
    }
}
