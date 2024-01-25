/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.sequence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.bonitasoft.engine.lock.LockService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class SequenceManagerImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private SequenceMappingProvider sequenceMappingProvider;
    @Mock
    private DataSource dataSource;
    @Mock
    private TenantSequenceManagerImpl tenantSequenceManager;
    @Mock
    private LockService lockService;

    private SequenceManagerImpl sequenceManager;

    private AtomicInteger callsToCreateTenantSequenceManager = new AtomicInteger();

    private static final long TENANTID = 1;

    @Before
    public void before() {
        sequenceManager = new SequenceManagerImpl(lockService, sequenceMappingProvider, dataSource, 2, 1, 1) {

            @Override
            TenantSequenceManagerImpl createTenantSequenceManager(long tenantId) {
                callsToCreateTenantSequenceManager.incrementAndGet();
                return tenantSequenceManager;
            }
        };
    }

    @Test
    public void should_call_get_next_id_on_the_tenant_sequence_manager() throws Exception {
        doReturn(500L).when(tenantSequenceManager).getNextId("someEntity");

        long nextId = sequenceManager.getNextId("someEntity", TENANTID);

        assertThat(nextId).isEqualTo(500L);
    }

    @Test
    public void should_create_tenantSequenceManager_once_per_tenant() throws Exception {
        sequenceManager.getNextId("someEntity", 1L);
        sequenceManager.getNextId("someEntity", 1L);
        sequenceManager.getNextId("someEntity", 2L);
        sequenceManager.getNextId("someEntity", 2L);
        sequenceManager.getNextId("someEntity", 1L);

        //2 tenants
        assertThat(callsToCreateTenantSequenceManager.get()).isEqualTo(2);
    }

}
