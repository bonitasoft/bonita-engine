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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class TenantSequenceManagerImpl {

    static final String SEQUENCE = "SEQUENCE";

    static final String NEXTID = "nextid";

    static final String SELECT_BY_ID = "SELECT * FROM sequence WHERE tenantid = ? AND id = ?";

    static final String UPDATE_SEQUENCE = "UPDATE sequence SET nextId = ? WHERE tenantid = ? AND id = ?";

    private final Long tenantId;

    private final Map<Long, Integer> sequenceIdToRangeSize;

    // Map of available IDs: key=sequenceId, value = nextAvailableId to be assigned to a new entity of the given sequence
    private final Map<Long, Long> nextAvailableIds = new HashMap<Long, Long>();

    // Map of lastId that can be consumed for a given sequence
    private final Map<Long, Long> lastIdInRanges = new HashMap<Long, Long>();

    // Map of sequenceId, mutex
    private static final Map<Long, Object> SEQUENCE_MUTEXS = new HashMap<Long, Object>();

    private final Map<String, Long> classNameToSequenceId;

    private final int retries;

    private final int delay;

    private final int delayFactor;

    private final DataSource datasource;

    private final LockService lockService;

    public TenantSequenceManagerImpl(final long tenantId, final LockService lockService, final Map<Long, Integer> sequenceIdToRangeSize,
            final Map<String, Long> classNameToSequenceId,
            final DataSource datasource, final int retries, final int delay, final int delayFactor) {
        this.tenantId = tenantId;
        this.lockService = lockService;
        this.sequenceIdToRangeSize = sequenceIdToRangeSize;
        this.classNameToSequenceId = classNameToSequenceId;
        this.retries = retries;
        this.delay = delay;
        this.delayFactor = delayFactor;
        this.datasource = datasource;

        for (final Long sequenceId : classNameToSequenceId.values()) {
            SEQUENCE_MUTEXS.put(sequenceId, new TenantSequenceManagerImplMutex());
            nextAvailableIds.put(sequenceId, 0L);
            lastIdInRanges.put(sequenceId, -1L);
        }
    }

    private static final class TenantSequenceManagerImplMutex {

    }

    public long getNextId(final String entityName) throws SObjectNotFoundException {
        final Long sequenceId = classNameToSequenceId.get(entityName);
        if (sequenceId == null) {
            throw new SObjectNotFoundException("No sequence id found for " + entityName);
        }
        final Object sequenceMutex = SEQUENCE_MUTEXS.get(sequenceId);
        synchronized (sequenceMutex) {
            Long nextAvailableId = nextAvailableIds.get(sequenceId);
            final Long lastIdInRange = lastIdInRanges.get(sequenceId);
            if (nextAvailableId > lastIdInRange) {
                // No available IF in the range this sequence can consume, we need to get a new range and calculate a new nextAvailableId
                setNewRange(sequenceId);
                nextAvailableId = nextAvailableIds.get(sequenceId);
            }
            nextAvailableIds.put(sequenceId, nextAvailableId + 1);

            return nextAvailableId;
        }
    }

    private void setNewRange(final long sequenceId) throws SObjectNotFoundException {
        BonitaLock lock;
        try {
            lock = lockService.lock(sequenceId, SEQUENCE, tenantId);

            try {

                int attempt = 1;
                long sleepTime = delay;
                while (attempt <= retries) {
                    if (attempt > 1) {
                        System.err.println("retrying... #" + attempt);
                    }
                    Connection connection = null;
                    try {
                        connection = datasource.getConnection();
                        connection.setAutoCommit(false);

                        // we have reach the maximum in this range
                        final long nextAvailableId = selectById(connection, sequenceId, tenantId);
                        nextAvailableIds.put(sequenceId, nextAvailableId);

                        final long nextSequenceId = nextAvailableId + sequenceIdToRangeSize.get(sequenceId);
                        updateSequence(connection, nextSequenceId, tenantId, sequenceId);
                        lastIdInRanges.put(sequenceId, nextSequenceId - 1);

                        connection.commit();
                        return;
                    } catch (final SObjectNotFoundException t) {
                        // Not found needs no retry.
                        attempt = retries + 1; // To exit the loop
                        try {
                            connection.rollback();
                        } catch (final SQLException e) {
                            e.printStackTrace();
                            // do nothing
                        }
                        throw t;
                    } catch (final Exception t) {
                        attempt++;
                        try {
                            connection.rollback();
                        } catch (final SQLException e) {
                            e.printStackTrace();
                            // do nothing
                        }
                        manageException(sleepTime, t);
                        sleepTime *= delayFactor;
                    } finally {
                        if (connection != null) {
                            try {
                                connection.close();
                            } catch (final SQLException e) {
                                // Can't do anything...
                            }
                        }
                    }
                }
            } finally {
                lockService.unlock(lock, tenantId);
            }
        } catch (SLockException e1) {
            throw new SObjectNotFoundException(
                    "Unable to get a sequence id for " + sequenceId, e1);
        }
        throw new SObjectNotFoundException(
                "Unable to get a sequence id for " + sequenceId);
    }

    protected void updateSequence(final Connection connection, final long nextSequenceId, final long tenantId, final long id)
            throws SQLException {
        PreparedStatement updateSequencePreparedStatement = connection.prepareStatement(UPDATE_SEQUENCE);
        try {
            updateSequencePreparedStatement.setObject(1, nextSequenceId);
            updateSequencePreparedStatement.setObject(2, tenantId);
            updateSequencePreparedStatement.setObject(3, id);
            updateSequencePreparedStatement.executeUpdate();
        } finally {
            if (updateSequencePreparedStatement != null) {
                updateSequencePreparedStatement.close();
            }
        }
    }

    protected long selectById(final Connection connection, final long id, final long tenantId) throws SQLException, SObjectNotFoundException {
        PreparedStatement selectByIdPreparedStatement = null;
        ResultSet resultSet = null;
        try {
            selectByIdPreparedStatement = connection.prepareStatement(SELECT_BY_ID);
            selectByIdPreparedStatement.setLong(1, tenantId);
            selectByIdPreparedStatement.setLong(2, id);
            resultSet = selectByIdPreparedStatement.executeQuery();
            return getNextId(id, tenantId, resultSet);
        } finally {
            if (selectByIdPreparedStatement != null) {
                selectByIdPreparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private long getNextId(final long id, final long tenantId, final ResultSet resultSet) throws SQLException, SObjectNotFoundException {
        try {
            if (resultSet.next()) {
                final long nextId = resultSet.getLong(NEXTID);

                if (resultSet.wasNull()) {
                    throw new SQLException("Did not expect a null value for the column " + NEXTID);
                }

                if (resultSet.next()) {
                    throw new SQLException("Did not expect more than one value for tenantId:" + tenantId + " id: " + id);
                }

                return nextId;
            }
        } finally {
            closeResultSet(resultSet);
        }
        throw new SObjectNotFoundException("Found no row for tenantId:" + tenantId + " id: " + id);
    }

    private void closeResultSet(final ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (final SQLException e) {
            // can't do anything
        }
    }

    private static void manageException(final long sleepTime, final Exception t) {
        t.printStackTrace();
        System.err.println("Optimistic locking failed: " + t);
        System.err.println("Waiting " + sleepTime + " millis");
        try {
            Thread.sleep(sleepTime);
        } catch (final InterruptedException e) {
            System.err.println("Retry sleeping got interrupted");
        }
    }

}
