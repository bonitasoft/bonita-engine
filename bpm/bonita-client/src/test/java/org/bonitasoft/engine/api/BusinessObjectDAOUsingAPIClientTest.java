/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.api;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.bdm.BusinessObjectDaoCreationException;
import org.bonitasoft.engine.bdm.DummyDAO;
import org.bonitasoft.engine.bdm.DummyDAOImpl;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.session.APISession;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Nicolas Chabanoles on 30/11/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class BusinessObjectDAOUsingAPIClientTest {

    @Spy
    @InjectMocks
    APIClient client;

    @Mock
    APISession session;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void should_create_dao_throw_IllegalArgmumentException_for_null_interface() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("daoInterface is null");
        client.getDAO(null);
    }

    @Test
    public void should_create_dao_throw_IllegalArgmumentException_if_daoInterface_is_not_an_interface() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("DummyDAOImpl is not an interface");
        client.getDAO(DummyDAOImpl.class);
    }

    @Test
    public void should_create_dao_throw_BusinessObjectDaoCreationException_if_daoImpl_not_in_classpath() throws Exception {
        Mockito.doThrow(ClassNotFoundException.class).when(client).loadClass(BusinessObjectDAO.class);

        expectedEx.expect(BusinessObjectDaoCreationException.class);
        expectedEx.expectMessage("");

        client.getDAO(BusinessObjectDAO.class);
    }

    @Test
    public void should_create_dao_throw_BusinessObjectDaoCreationException_if_daoImpl_has_no_constructor_with_session() throws Exception {
        Mockito.doReturn(DummyDAOWithoutConstructorImpl.class).when(client).loadClass(Matchers.any(Class.class));

        expectedEx.expect(BusinessObjectDaoCreationException.class);
        expectedEx.expectMessage("");

        client.getDAO(DummyDAO.class);
    }

    @Test
    public void should_create_dao_return_implementation() throws Exception {
        DummyDAO daoInstance = client.getDAO(DummyDAO.class);
        Assertions.assertThat(daoInstance).isNotNull();
    }

    class DummyDAOWithoutConstructorImpl implements DummyDAO {

    }

}
