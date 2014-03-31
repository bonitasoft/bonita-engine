/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.command;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.service.TenantServiceAccessor;


/**
 * @author Romain Bioteau
 *
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
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        doReturn(bdrService).when(command).getBusinessDataRepository(tenantServiceAccessor);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
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
        
        command.execute(parameters, tenantServiceAccessor);

        verify(bdrService).findListByNamedQuery(queryName, resultClass, queryParameters);
    }
    
    @Test(expected=SCommandParameterizationException.class)
    public void should_Execute_throw_SCommandParameterizationException_if_no_query_name() throws Exception {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, null);
        command.execute(parameters, tenantServiceAccessor);
        parameters.put(ExecuteBDMQueryCommand.QUERY_NAME, "");
        command.execute(parameters, tenantServiceAccessor);
    }
    
    @Test(expected=SCommandParameterizationException.class)
    public void should_Execute_throw_SCommandParameterizationException_if_no_return_type() throws Exception {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, null);
        command.execute(parameters, tenantServiceAccessor);
        parameters.put(ExecuteBDMQueryCommand.RETURN_TYPE, "");
        command.execute(parameters, tenantServiceAccessor);
    }
    
    

}
