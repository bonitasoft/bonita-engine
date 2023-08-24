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

    static final String NEXT_ID = "nextid";
    static final String SELECT_BY_ID = "SELECT * FROM sequence WHERE id = ?";

    static final String UPDATE_SEQUENCE = "UPDATE sequence SET nextId = ? WHERE id = ?";

    private final Connection connection;

    public SequenceDAO(Connection connection) {
        this.connection = connection;
    }

    protected void updateSequence(long nextSequenceId, long id) throws SQLException {
        try (PreparedStatement updateSequencePreparedStatement = connection.prepareStatement(UPDATE_SEQUENCE)) {
            updateSequencePreparedStatement.setObject(1, nextSequenceId);
            updateSequencePreparedStatement.setObject(2, id);
            updateSequencePreparedStatement.executeUpdate();
        }
    }

    protected long selectById(long id) throws SQLException, SObjectNotFoundException {
        PreparedStatement selectByIdPreparedStatement = null;
        ResultSet resultSet = null;
        try {
            selectByIdPreparedStatement = connection.prepareStatement(SELECT_BY_ID);
            selectByIdPreparedStatement.setLong(1, id);
            resultSet = selectByIdPreparedStatement.executeQuery();
            return getNextId(id, resultSet);
        } finally {
            if (selectByIdPreparedStatement != null) {
                selectByIdPreparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private long getNextId(final long id, final ResultSet resultSet)
            throws SQLException, SObjectNotFoundException {
        try {
            if (resultSet.next()) {
                final long nextId = resultSet.getLong(NEXT_ID);

                if (resultSet.wasNull()) {
                    throw new SQLException("Did not expect a null value for the column " + NEXT_ID);
                }

                if (resultSet.next()) {
                    throw new SQLException(
                            "Did not expect more than one value for id: " + id);
                }

                return nextId;
            }
        } finally {
            closeResultSet(resultSet);
        }
        throw new SObjectNotFoundException("Found no row for id: " + id);
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
