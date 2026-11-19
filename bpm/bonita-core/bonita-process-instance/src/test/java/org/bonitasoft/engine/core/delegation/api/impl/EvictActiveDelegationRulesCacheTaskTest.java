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

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Set;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvictActiveDelegationRulesCacheTaskTest {

    @Mock
    private CacheService cacheService;

    @Test
    void call_removes_each_delegate_entry_from_the_local_region() throws Exception {
        //given — the task as it arrives on a remote node, with that node's CacheService injected
        final EvictActiveDelegationRulesCacheTask task = new EvictActiveDelegationRulesCacheTask(Set.of(20L, 30L));
        task.setCacheService(cacheService);

        //when
        task.call();

        //then
        verify(cacheService).remove(DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE, 20L);
        verify(cacheService).remove(DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE, 30L);
        verifyNoMoreInteractions(cacheService);
    }

    @Test
    void call_continues_evicting_remaining_delegates_when_one_removal_fails() throws Exception {
        //given — evicting delegate 20 fails on this node
        final EvictActiveDelegationRulesCacheTask task = new EvictActiveDelegationRulesCacheTask(Set.of(20L, 30L));
        task.setCacheService(cacheService);
        // lenient: the set's iteration order is unspecified, so the other (unstubbed) removal may run first
        lenient().doThrow(new SCacheException("boom")).when(cacheService)
                .remove(DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE, 20L);

        //when — the failure is swallowed, not propagated, so the batch is not aborted
        task.call();

        //then — the other delegate is still evicted
        verify(cacheService).remove(DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE, 20L);
        verify(cacheService).remove(DelegationRuleServiceImpl.ACTIVE_DELEGATION_RULES_CACHE, 30L);
        verifyNoMoreInteractions(cacheService);
    }
}
