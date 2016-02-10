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

package org.bonitasoft.engine.business.data.proxy;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerLazyLoaderTest {

    public class Addresses implements Serializable {

        private static final long serialVersionUID = 1L;

        public String getCity() {
            return "city";
        }
    }

    public class Employee {

        public Employee() {

        }

        public List<Addresses> getAddresses() {
            return new ArrayList<>();
        }

        public String getName() {
            return "name";
        }

    }

    @Mock
    private BusinessDataRepository businessDataRepository;

    private ServerLazyLoader serverLazyLoader;

    final long persistenceId = 22L;

    Employee employee = new Employee();

    @Before
    public void setUp() throws Exception {
        serverLazyLoader = spy(new ServerLazyLoader(businessDataRepository));

    }

    @Test
    public void should_load_list_of_objects() throws Exception {
        //given
        final Method method = employee.getClass().getMethod("getAddresses");

        //when
        serverLazyLoader.load(method, persistenceId);

        final String queryName = "Addresses.findAddressesByEmployeePersistenceId";
        final Class<? extends Serializable> resultClass = Addresses.class;
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("persistenceId", persistenceId);
        final int startIndex = 0;
        final int maxResults = Integer.MAX_VALUE;

        //then
        verify(businessDataRepository).findListByNamedQuery(queryName, resultClass, parameters, startIndex, maxResults);
        verify(businessDataRepository, never()).findByNamedQuery(queryName, resultClass, parameters);

    }

    @Test
    public void should_load_single_object() throws Exception {
        //given
        final String queryName = "String.findNameByEmployeePersistenceId";
        final Class<? extends Serializable> resultClass = String.class;
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("persistenceId", persistenceId);
        final int startIndex = 0;
        final int maxResults = Integer.MAX_VALUE;
        final Method method = employee.getClass().getMethod("getName");

        //when
        serverLazyLoader.load(method, persistenceId);

        //then
        verify(businessDataRepository, never()).findListByNamedQuery(queryName, resultClass, parameters, startIndex, maxResults);
        verify(businessDataRepository).findByNamedQuery(queryName, resultClass, parameters);

    }

    @Test(expected = RuntimeException.class)
    public void should_load_single_object_throw_exception() throws Exception {
        final String queryName = "String.findNameByEmployeePersistenceId";
        final Class<? extends Serializable> resultClass = String.class;
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("persistenceId", persistenceId);
        final Method method = employee.getClass().getMethod("getName");

        //given
        doThrow(NonUniqueResultException.class).when(businessDataRepository).findByNamedQuery(queryName, resultClass, parameters);

        //when
        serverLazyLoader.load(method, persistenceId);

        //then exception
    }

}
