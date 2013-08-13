/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class SequenceManagerImpl implements SequenceManager {

    private static final String NEXTID = "nextid";

    private static final String SELECT_BY_ID = "SELECT * FROM sequence WHERE tenantid = ? AND id = ?";

    private static final String UPDATE_SEQUENCE = "UPDATE sequence SET nextId = ? WHERE tenantid = ? AND id = ?";

    private final Map<Long, Integer> rangeSizes;

    // Map of available IDs: key=sequenceId, value = nextAvailableId to be assigned to a new entity of the given sequence
    private Map<Long, Map<Long, Long>> nextAvailableIds;

    // Map of lastId that can be consumed for a given sequence
    private Map<Long, Map<Long, Long>> lastIdInRanges;

    private static final Map<Long, Object> sequenceMutexs = new HashMap<Long, Object>();

    private final int defaultRangeSize;

    private final Map<String, Long> sequencesMappings;

    private static final Object newRangeMutex = new Object();

    private final int retries;

    private final int delay;

    private final int delayFactor;

    private final DataSource datasource;

    public SequenceManagerImpl(final Map<Long, Integer> rangeSizes, final int defaultRangeSize, final Map<String, Long> sequencesMappings,
            final DataSource datasource, final int retries, final int delay, final int delayFactor) {
        this.defaultRangeSize = defaultRangeSize;
        this.rangeSizes = rangeSizes;
        this.sequencesMappings = sequencesMappings;
        this.retries = retries;
        this.delay = delay;
        this.delayFactor = delayFactor;
        this.nextAvailableIds = new HashMap<Long, Map<Long, Long>>();
        this.lastIdInRanges = new HashMap<Long, Map<Long, Long>>();
        this.datasource = datasource;
    }

    @Override
    public void reset() {
        nextAvailableIds = new HashMap<Long, Map<Long, Long>>();
        lastIdInRanges = new HashMap<Long, Map<Long, Long>>();
    }

    private long getNextAvailableId(final long sequenceId, final long tenantId) {
        final Map<Long, Long> nextAvailableIds = getNextAvailableIdsMapForTenant(tenantId);
        if (!nextAvailableIds.containsKey(sequenceId)) {
            nextAvailableIds.put(sequenceId, 0L);
        }
        return nextAvailableIds.get(sequenceId);
    }

    /**
     * @param tenantId
     * @return
     */
    private Map<Long, Long> getNextAvailableIdsMapForTenant(final long tenantId) {
        if (!nextAvailableIds.containsKey(tenantId)) {
            nextAvailableIds.put(tenantId, new HashMap<Long, Long>());
        }
        return nextAvailableIds.get(tenantId);
    }

    private void setNextAvailableId(final long sequenceId, final long nextAvailableId, final long tenantId) {
        getNextAvailableIdsMapForTenant(tenantId).put(sequenceId, nextAvailableId);
    }

    private long getLastIdInRange(final long sequenceId, final long tenantId) {
        final Map<Long, Long> lastIdInRanges = getLastIdInRangeMapForTenant(tenantId);
        if (!lastIdInRanges.containsKey(sequenceId)) {
            lastIdInRanges.put(sequenceId, -1L);
        }
        return lastIdInRanges.get(sequenceId);
    }

    /**
     * @param tenantId
     * @return
     */
    private Map<Long, Long> getLastIdInRangeMapForTenant(final long tenantId) {
        if (!lastIdInRanges.containsKey(tenantId)) {
            lastIdInRanges.put(tenantId, new HashMap<Long, Long>());
        }
        return lastIdInRanges.get(tenantId);
    }

    private void setLastIdInRange(final long sequenceId, final long lastIdInRange, final long tenantId) {
        getLastIdInRangeMapForTenant(tenantId).put(sequenceId, lastIdInRange);
    }

    private static synchronized Object getSequenceMutex(final Long sequenceId) {
        if (!sequenceMutexs.containsKey(sequenceId)) {
            sequenceMutexs.put(sequenceId, new Object());
        }
        return sequenceMutexs.get(sequenceId);
    }

    @Override
    public long getNextId(final String entityName, final long tenantId) throws SObjectNotFoundException, SObjectModificationException {
        final Long sequenceId = sequencesMappings.get(entityName);
        if (sequenceId == null) {
            throw new SObjectNotFoundException("No sequence id found for " + entityName);
        }
        final Object sequenceMutex = getSequenceMutex(sequenceId);
        synchronized (sequenceMutex) {
            final long nextAvailableId = getNextAvailableId(sequenceId, tenantId);
            final long lastIdInRange = getLastIdInRange(sequenceId, tenantId);
            // System.err.println("***** getting new ID for sequence: " +
            // sequenceId + ", nextAvailableId= " + nextAvailableId +
            // ", lastIdInRange=" +
            // lastIdInRange);
            if (nextAvailableId > lastIdInRange) {
                // System.err.println("nextAvailableId > lastIdInRange, settingNewRange..");
                // no available IF in the range this sequence can consume, we
                // need to get a new range and calculate a new nextAvailableId
                setNewRange(sequenceId, tenantId);
                return getNextId(entityName, tenantId);
            }
            // System.err.println("***** getting new ID for sequence: " +
            // sequenceId + ", setting nextAvailableId to= " +
            // (nextAvailableId+1) +
            // " and returning: " + nextAvailableId);
            setNextAvailableId(sequenceId, nextAvailableId + 1, tenantId);

            return nextAvailableId;
        }
    }

    private void setNewRange(final long sequenceId, final long tenantId) throws SObjectNotFoundException {
        synchronized (newRangeMutex) {
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
                    final long nextAvailableId = selectById(connection,
                            sequenceId, tenantId);
                    setNextAvailableId(sequenceId, nextAvailableId, tenantId);

                    final long nextSequenceId = nextAvailableId
                            + getRangeSize(sequenceId);
                    updateSequence(connection, nextSequenceId, tenantId,
                            sequenceId);
                    setLastIdInRange(sequenceId, nextSequenceId - 1, tenantId);

                    connection.commit();
                    return;
                } catch (final Throwable t) {
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
                        } catch (SQLException e) {
                            // Can't do anything...
                        }
                    }
                }
            }
            throw new SObjectNotFoundException(
                    "Unable to get a sequence id for " + sequenceId);
        }
    }

    int getRangeSize(final long sequenceId) {
        final Integer rangeSize = rangeSizes.get(sequenceId);
        return rangeSize != null ? rangeSize : defaultRangeSize;
    }

    protected void updateSequence(Connection connection, final long nextSequenceId, final long tenantId, final long id)
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

    protected long selectById(Connection connection, final long id, final long tenantId) throws SQLException, SObjectNotFoundException {
        PreparedStatement selectByIdPreparedStatement = null;
        try {
            selectByIdPreparedStatement = connection.prepareStatement(SELECT_BY_ID);
            selectByIdPreparedStatement.setLong(1, tenantId);
            selectByIdPreparedStatement.setLong(2, id);
            final ResultSet resultSet = selectByIdPreparedStatement.executeQuery();
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
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                } catch (final SQLException e) {
                    // can't do anything
                }
            }
            throw new SObjectNotFoundException("Found no row for tenantId:" + tenantId + " id: " + id);
        } finally {
            if (selectByIdPreparedStatement != null) {
                selectByIdPreparedStatement.close();
            }
        }
    }

    private static void manageException(final long sleepTime, final Throwable t) {
        t.printStackTrace();
        System.err.println("Optimistic locking failed: " + t);
        System.err.println("Waiting " + sleepTime + " millis");
        try {
            Thread.sleep(sleepTime);
        } catch (final InterruptedException e) {
            System.err.println("Retry sleeping got interrupted");
        }
    }

    @Override
    public void clear() {
        nextAvailableIds.clear();
        lastIdInRanges.clear();
    }

    @Override
    public void close() {
    }

    @Override
    public void clear(final long tenantId) {
        if (nextAvailableIds.containsKey(tenantId)) {
            nextAvailableIds.get(tenantId).clear();
        }
        if (lastIdInRanges.containsKey(tenantId)) {
            lastIdInRanges.get(tenantId).clear();
        }
    }

}
