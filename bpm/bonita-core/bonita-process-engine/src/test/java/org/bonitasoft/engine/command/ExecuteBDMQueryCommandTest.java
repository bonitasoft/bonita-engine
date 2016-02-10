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
package org.bonitasoft.engine.command;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteBDMQueryCommandTest {

    @Spy
    private ExecuteBDMQueryCommand command;

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() {
        doReturn(bdrService).when(command).getBusinessDataRepository(tenantServiceAccessor);
    }

    @Test
    public void should_Execute_call_findByNamedQuery() throws Exception {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        Class<? extends Serializable> resultClass = String.class;
        String queryName = "testQuery";
        Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        boolean returnsList = false;

        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, queryName);
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, String.class.getName());
        parameters.put(ExecuteBDMQueryCommand.QUERY_PARAMETERS, (Serializable) queryParameters);
        parameters.put(ExecuteBDMQueryCommand.RETURNS_LIST, returnsList);

        command.execute(parameters, tenantServiceAccessor);

        verify(bdrService).findByNamedQuery(queryName, resultClass, queryParameters);
    }

    @Test
    public void should_Execute_call_findListByNamedQuery() throws Exception {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        Class<? extends Serializable> resultClass = String.class;
        String queryName = "testQuery";
        Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        boolean returnsList = true;

        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, queryName);
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, String.class.getName());
        parameters.put(ExecuteBDMQueryCommand.QUERY_PARAMETERS, (Serializable) queryParameters);
        parameters.put(ExecuteBDMQueryCommand.RETURNS_LIST, returnsList);
        parameters.put(ExecuteBDMQueryCommand.START_INDEX, 0);
        parameters.put(ExecuteBDMQueryCommand.MAX_RESULTS, 10);

        command.execute(parameters, tenantServiceAccessor);

        verify(bdrService).findListByNamedQuery(queryName, resultClass, queryParameters, 0, 10);
    }

    @Test(expected = SCommandParameterizationException.class)
    public void should_Execute_throw_SCommandParameterizationException_if_no_query_name() throws Exception {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, null);
        command.execute(parameters, tenantServiceAccessor);
        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, "");
        command.execute(parameters, tenantServiceAccessor);
    }

    @Test(expected = SCommandParameterizationException.class)
    public void should_Execute_throw_SCommandParameterizationException_if_no_return_type() throws Exception {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, null);
        command.execute(parameters, tenantServiceAccessor);
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, "");
        command.execute(parameters, tenantServiceAccessor);
    }

}
