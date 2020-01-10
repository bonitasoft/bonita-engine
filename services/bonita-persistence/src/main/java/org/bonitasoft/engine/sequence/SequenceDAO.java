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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;

public class SequenceDAO {

    static final String NEXTID = "nextid";
    static final String SELECT_BY_ID = "SELECT * FROM sequence WHERE tenantid = ? AND id = ?";

    static final String UPDATE_SEQUENCE = "UPDATE sequence SET nextId = ? WHERE tenantid = ? AND id = ?";

    private Connection connection;
    private Long tenantId;

    public SequenceDAO(Connection connection, Long tenantId) {
        this.connection = connection;
        this.tenantId = tenantId;
    }

    protected void updateSequence(long nextSequenceId, long id) throws SQLException {
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

    protected long selectById(long id) throws SQLException, SObjectNotFoundException {
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

    private long getNextId(final long id, final long tenantId, final ResultSet resultSet)
            throws SQLException, SObjectNotFoundException {
        try {
            if (resultSet.next()) {
                final long nextId = resultSet.getLong(NEXTID);

                if (resultSet.wasNull()) {
                    throw new SQLException("Did not expect a null value for the column " + NEXTID);
                }

                if (resultSet.next()) {
                    throw new SQLException(
                            "Did not expect more than one value for tenantId:" + tenantId + " id: " + id);
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

}
