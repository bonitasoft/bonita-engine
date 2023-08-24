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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.engine.sequence.SequenceDAO.NEXT_ID;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class SequenceDAOTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @InjectMocks
    private SequenceDAO sequenceDAO;

    @Before
    public void before() {
        sequenceDAO = new SequenceDAO(connection);
    }

    @Test
    public void should_getNextId_from_database() throws Exception {
        doReturn(preparedStatement).when(connection).prepareStatement(SequenceDAO.SELECT_BY_ID);
        doReturn(resultSet).when(preparedStatement).executeQuery();
        doReturn(true, false).when(resultSet).next();
        doReturn(2349L).when(resultSet).getLong(NEXT_ID);

        long nextId = sequenceDAO.selectById(123L);

        verify(preparedStatement).setLong(1, 123L);
        assertThat(nextId).isEqualTo(2349);
    }

    @Test
    public void should_getNextId_fail_when_there_is_more_than_one_result() throws Exception {
        doReturn(preparedStatement).when(connection).prepareStatement(SequenceDAO.SELECT_BY_ID);
        doReturn(resultSet).when(preparedStatement).executeQuery();
        doReturn(true, true, false).when(resultSet).next();

        assertThatThrownBy(() -> sequenceDAO.selectById(123L))
                .isInstanceOf(SQLException.class)
                .hasMessage("Did not expect more than one value for id: 123");
    }

    @Test
    public void should_getNextId_fail_when_there_is_no_result() throws Exception {
        doReturn(preparedStatement).when(connection).prepareStatement(SequenceDAO.SELECT_BY_ID);
        doReturn(resultSet).when(preparedStatement).executeQuery();
        doReturn(false).when(resultSet).next();

        assertThatThrownBy(() -> sequenceDAO.selectById(123L))
                .isInstanceOf(SObjectNotFoundException.class)
                .hasMessage("Found no row for id: 123");
    }

    @Test
    public void should_getNextId_fail_when_the_result_is_null() throws Exception {
        doReturn(preparedStatement).when(connection).prepareStatement(SequenceDAO.SELECT_BY_ID);
        doReturn(resultSet).when(preparedStatement).executeQuery();
        doReturn(true, false).when(resultSet).next();
        doReturn(true).when(resultSet).wasNull();

        assertThatThrownBy(() -> sequenceDAO.selectById(123L))
                .isInstanceOf(SQLException.class)
                .hasMessage("Did not expect a null value for the column nextid");
    }

    @Test
    public void should_update_database_with_next_sequence_id() throws Exception {
        doReturn(preparedStatement).when(connection).prepareStatement(SequenceDAO.UPDATE_SEQUENCE);
        doReturn(1).when(preparedStatement).executeUpdate();

        sequenceDAO.updateSequence(11233L, 123L);

        verify(preparedStatement).setObject(1, 11233L);
        verify(preparedStatement).setObject(2, 123L);
    }
}
