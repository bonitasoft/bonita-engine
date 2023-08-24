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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.lock.SLockTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class TenantSequenceManagerImpl {

    private static Logger logger = LoggerFactory.getLogger(TenantSequenceManagerImpl.class);

    static final String SEQUENCE = "SEQUENCE";

    private final Long tenantId;
    private final Map<Long, SequenceRange> sequences = new HashMap<>();
    private final Map<Long, Integer> sequenceIdToRangeSize;
    private final Map<String, Long> classNameToSequenceId;

    private final int retries;

    private final int delay;

    private final int delayFactor;

    private final DataSource datasource;

    private final LockService lockService;

    public TenantSequenceManagerImpl(final long tenantId, final LockService lockService,
            final Map<Long, Integer> sequenceIdToRangeSize,
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
    }

    public long getNextId(final String entityName) throws SObjectNotFoundException {
        final Long sequenceId = getSequenceId(entityName);
        SequenceRange sequence = getSequence(sequenceId);
        Optional<Long> nextAvailableId = sequence.getNextAvailableId();
        if (nextAvailableId.isPresent()) {
            return nextAvailableId.get();
        }
        //synchronize on the sequence object itself (we will read/update only on this one)
        synchronized (sequence) {
            nextAvailableId = sequence.getNextAvailableId();
            int loopCounter = 0;
            // set a max number of retries to 100:
            while (!nextAvailableId.isPresent() && loopCounter < 100) {
                if (loopCounter > 0) {
                    logger.debug("Could not get an Id after updating to next range. Retrying...");
                }
                sequence.updateToNextRange(setNewRange(sequenceId));
                nextAvailableId = sequence.getNextAvailableId();
                loopCounter++;
            }
            return nextAvailableId.orElseThrow(
                    () -> new IllegalStateException("No new available id found for sequence " + entityName));
        }
    }

    private SequenceRange getSequence(Long sequenceId) {
        if (!sequences.containsKey(sequenceId)) {
            synchronized (this) {
                if (!sequences.containsKey(sequenceId)) {
                    sequences.put(sequenceId, new SequenceRange(sequenceIdToRangeSize.get(sequenceId)));
                }
            }
        }
        return sequences.get(sequenceId);
    }

    private Long getSequenceId(String entityName) throws SObjectNotFoundException {
        final Long sequenceId = classNameToSequenceId.get(entityName);
        if (sequenceId == null) {
            throw new SObjectNotFoundException("No sequence id found for " + entityName);
        }
        return sequenceId;
    }

    /**
     * get the next available id of a sequence and update in database its value
     *
     * @return the next available id of the sequence
     */
    private long setNewRange(final long sequenceId) throws SObjectNotFoundException {
        BonitaLock lock = createLock(sequenceId);
        Exception lastException = null;
        try {
            int attempt = 1;
            long sleepTime = delay;
            while (attempt <= retries) {
                if (attempt > 1) {
                    logger.info("Retry #{} to retrieve next sequence id of sequence {}", attempt, sequenceId);
                }
                Connection connection = getConnection();
                try {
                    connection.setAutoCommit(false);
                    SequenceDAO sequenceDAO = createDao(connection);
                    long nextAvailableId = sequenceDAO.selectById(sequenceId);
                    sequenceDAO.updateSequence(nextAvailableId + sequenceIdToRangeSize.get(sequenceId), sequenceId);
                    connection.commit();
                    return nextAvailableId;
                } catch (final SObjectNotFoundException t) {
                    rollback(connection);
                    throw t;
                } catch (final Exception t) {
                    attempt++;
                    rollback(connection);
                    lastException = t;
                    manageException(attempt, sleepTime, t);
                    sleepTime *= delayFactor;
                } finally {
                    close(connection);
                }
            }
        } finally {
            unlock(lock);
        }

        throw new SObjectNotFoundException(
                "Unable to get a sequence id for " + sequenceId, lastException);
    }

    private void unlock(BonitaLock lock) {
        try {
            lockService.unlock(lock, tenantId);
        } catch (SLockException e) {
            throw new SBonitaRuntimeException(
                    "Unable to unlock the lock require to get next id of sequences from database", e);
        }
    }

    private BonitaLock createLock(long sequenceId) {
        BonitaLock lock;
        try {
            lock = lockService.lock(sequenceId, SEQUENCE, tenantId);
        } catch (SLockException | SLockTimeoutException e) {
            throw new SBonitaRuntimeException(
                    "Unable to acquire lock in order to update get the next id from database of the sequence "
                            + sequenceId,
                    e);
        }
        return lock;
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (final SQLException e) {
            throw new SBonitaRuntimeException(
                    "Next id of sequence correctly updated, but unable to close the connection", e);
        }
    }

    private Connection getConnection() {
        try {
            return datasource.getConnection();
        } catch (SQLException e) {
            throw new SBonitaRuntimeException("Unable to acquire connection to retrieve next id of the sequence", e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new SBonitaRuntimeException(
                    "Unable to rollback the transaction that get/update next sequence id from database", e);
        }
    }

    SequenceDAO createDao(Connection connection) {
        return new SequenceDAO(connection);
    }

    private static void manageException(int attempt, final long sleepTime, final Exception t) {
        logger.error("Unable to retrieve and update sequence in database because: {}." +
                "( attempt #{} ). Will sleep {} millis before retrying. ", t.getMessage(), attempt, sleepTime);
        logger.debug("Cause:", t);
        try {
            Thread.sleep(sleepTime);
        } catch (final InterruptedException ignored) {
            logger.error("Interrupted while sleeping before retry");
        }
    }

}
