/*******************************************************************************
 * Copyright (C) 2013 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandParameterizationException;
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
