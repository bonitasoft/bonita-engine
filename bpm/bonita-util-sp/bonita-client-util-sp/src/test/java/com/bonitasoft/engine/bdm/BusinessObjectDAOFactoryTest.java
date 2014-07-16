/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.bonitasoft.engine.session.APISession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class BusinessObjectDAOFactoryTest {

    @Mock
    private APISession session;

    @Spy
    private BusinessObjectDAOFactory factory;

    @Test(expected = IllegalArgumentException.class)
    public void should_create_dao_throw_IllegalArgmumentException_for_null_session() throws Exception {
        factory.createDAO(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_create_dao_throw_IllegalArgmumentException_for_null_interface() throws Exception {
        factory.createDAO(session, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_create_dao_throw_IllegalArgmumentException_if_daoInterface_is_not_an_interface() throws Exception {
        factory.createDAO(session, DummyDAOImpl.class);
    }

    @Test(expected = BusinessObjectDaoCreationException.class)
    public void should_create_dao_throw_BusinessObjectDaoCreationException_if_daoImpl_not_in_classpath() throws Exception {
        doThrow(ClassNotFoundException.class).when(factory).loadClass(BusinessObjectDAO.class.getName());
        factory.createDAO(session, BusinessObjectDAO.class);
    }

    @Test(expected = BusinessObjectDaoCreationException.class)
    public void should_create_dao_throw_BusinessObjectDaoCreationException_if_daoImpl_has_no_constructor_with_session() throws Exception {
        doReturn(DummyDAOWithoutConstructorImpl.class).when(factory).loadClass(anyString());
        factory.createDAO(session, DummyDAO.class);
    }

    @Test
    public void should_create_dao_return_implementation() throws Exception {
        DummyDAO daoInstance = factory.createDAO(session, DummyDAO.class);
        assertThat(daoInstance).isNotNull();
    }

    class DummyDAOWithoutConstructorImpl implements DummyDAO {

    }

}
