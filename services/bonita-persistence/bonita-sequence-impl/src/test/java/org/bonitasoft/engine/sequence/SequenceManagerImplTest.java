/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class SequenceManagerImplTest {

    private static final long SEQUENCE_ID = 125L;

    private static final int RANGE_SIZE = 3;

    private final class TrueThenFalse implements Answer<Boolean> {

        int i = 0;

        @Override
        public Boolean answer(final InvocationOnMock invocation) {
            i++;
            return i % 2 != 0;
        }
    }

    private final Connection connection = mock(Connection.class);

    private LockService lockService;

    private DataSource datasource;

    private SequenceManagerImpl sequenceManager;

    private PreparedStatement preparedStatement;

    private ResultSet resultSet;

    private BonitaLock lock;

    private static final long TENANTID = 1;

    @Before
    public void before() throws SQLException, SLockException {
        lockService = mock(LockService.class);
        datasource = mock(DataSource.class);
        lock = mock(BonitaLock.class);
        when(lockService.lock(anyLong(), anyString(), eq(TENANTID))).thenReturn(lock);
        final SequenceMappingProvider sequenceMappingProvider = new SequenceMappingProvider();
        sequenceMappingProvider.setSequenceMappings(Collections.singletonList(new SequenceMapping("myClass", SEQUENCE_ID, RANGE_SIZE)));
        sequenceManager = new SequenceManagerImpl(lockService, sequenceMappingProvider, datasource, 2, 1,
                1);
        when(datasource.getConnection()).thenReturn(connection);
        preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(TenantSequenceManagerImpl.SELECT_BY_ID)).thenReturn(preparedStatement);
        when(connection.prepareStatement(TenantSequenceManagerImpl.UPDATE_SEQUENCE)).thenReturn(preparedStatement);
        resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    public void testGetNextId() throws Exception {
        when(resultSet.next()).thenAnswer(new TrueThenFalse());
        when(resultSet.getLong(TenantSequenceManagerImpl.NEXTID)).thenReturn(110L, 310L);
        // first call to DB, return what is in DB
        // before we hit range size the sequence manager do +1 on next id
        for (int i = 0; i < RANGE_SIZE; i++) {
            assertEquals(i + 110, sequenceManager.getNextId("myClass", 1));
        }
        // after range size it calls DB again
        assertEquals(310, sequenceManager.getNextId("myClass", 1));
    }

    @Test
    public void testGetNextIdDatabaseAccessIsInsideLock() throws Exception {
        when(resultSet.next()).thenAnswer(new TrueThenFalse());
        when(resultSet.getLong(TenantSequenceManagerImpl.NEXTID)).thenReturn(110L);
        InOrder inOrder = inOrder(lockService, connection);
        assertEquals(110, sequenceManager.getNextId("myClass", 1));
        inOrder.verify(lockService).lock(SEQUENCE_ID, TenantSequenceManagerImpl.SEQUENCE, TENANTID);
        inOrder.verify(connection).commit();
        inOrder.verify(lockService).unlock(lock, TENANTID);
    }

    @Test
    public void testGetNextIdRetry() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(TenantSequenceManagerImpl.NEXTID)).thenThrow(new SQLException("Haha")).thenReturn(110L);
        assertEquals(110, sequenceManager.getNextId("myClass", 1));
        verify(resultSet, times(2)).getLong(TenantSequenceManagerImpl.NEXTID);
        verify(lockService, times(1)).lock(anyLong(), anyString(), eq(TENANTID));
        verify(lockService, times(1)).unlock(lock, TENANTID);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void testGetNextIdRetryFail() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong(TenantSequenceManagerImpl.NEXTID)).thenThrow(new SQLException("e1"), new SQLException("e2"));
        assertEquals(110, sequenceManager.getNextId("myClass", 1));
    }

}
