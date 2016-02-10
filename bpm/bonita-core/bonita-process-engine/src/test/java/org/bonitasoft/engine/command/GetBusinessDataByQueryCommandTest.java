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

import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetBusinessDataByQueryCommandTest {

    private static final String PARAMETER_BUSINESS_DATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";
    private static final String PARAMETER_RETURN_TYPE = "com.company.model.Employee";
    public static final String PARAMETER_QUERY_NAME = "myQuery";
    public static final int PARAMETER_MAX_RESULTS = 10;
    public static final int PARAMETER_START_INDEX = 3;

    private GetBusinessDataByQueryCommand command;

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private BusinessDataService businessDataService;

    private Map<String, Serializable> commandParameters;
    private Map<String, Serializable> queryParameters;

    @Before
    public void setUp() throws Exception {
        command = new GetBusinessDataByQueryCommand();

        queryParameters = new HashMap<String, Serializable>();
        queryParameters.put("param", "value");

        commandParameters = new HashMap<String, Serializable>();
        commandParameters.put(GetBusinessDataByQueryCommand.QUERY_NAME, PARAMETER_QUERY_NAME);
        commandParameters.put(GetBusinessDataByQueryCommand.ENTITY_CLASS_NAME, PARAMETER_RETURN_TYPE);
        commandParameters.put(GetBusinessDataByQueryCommand.MAX_RESULTS, PARAMETER_MAX_RESULTS);
        commandParameters.put(GetBusinessDataByQueryCommand.START_INDEX, PARAMETER_START_INDEX);
        commandParameters.put(GetBusinessDataByQueryCommand.QUERY_PARAMETERS, (Serializable) queryParameters);
        commandParameters.put(BusinessDataCommandField.BUSINESS_DATA_URI_PATTERN, PARAMETER_BUSINESS_DATA_CLASS_URI_VALUE);
        when(tenantServiceAccessor.getBusinessDataService()).thenReturn(businessDataService);
    }

    @Test
    public void executeCommand_should_call_service() throws Exception {
        //when
        command.execute(commandParameters, tenantServiceAccessor);

        //then
        verify(businessDataService).getJsonQueryEntities(PARAMETER_RETURN_TYPE, PARAMETER_QUERY_NAME, queryParameters, PARAMETER_START_INDEX,
                PARAMETER_MAX_RESULTS,
                PARAMETER_BUSINESS_DATA_CLASS_URI_VALUE);
    }

    @Test(expected = SCommandExecutionException.class)
    public void executeCommand_should_throw_exception() throws Exception {
        //given
        doThrow(SBusinessDataRepositoryException.class).when(businessDataService).getJsonQueryEntities(PARAMETER_RETURN_TYPE, PARAMETER_QUERY_NAME,
                queryParameters, PARAMETER_START_INDEX, PARAMETER_MAX_RESULTS,
                PARAMETER_BUSINESS_DATA_CLASS_URI_VALUE);

        //when then exception
        command.execute(commandParameters, tenantServiceAccessor);
    }

    @Test
    public void executeCommand_should_check_return_type() throws Exception {
        String[] mandatoryParameters = { GetBusinessDataByQueryCommand.START_INDEX, GetBusinessDataByQueryCommand.MAX_RESULTS,
                GetBusinessDataByQueryCommand.QUERY_NAME, GetBusinessDataByQueryCommand.QUERY_PARAMETERS, GetBusinessDataByQueryCommand.ENTITY_CLASS_NAME };

        for (String mandatoryParameter : mandatoryParameters) {
            try {
                verify_command_checks_parameter(mandatoryParameter);
                fail("should throw exception");
            } catch (SCommandParameterizationException e) {
                //ok
            }
        }
    }

    private void verify_command_checks_parameter(String mandatoryParameter) throws Exception {
        //given
        commandParameters.remove(mandatoryParameter);

        //when then exception
        command.execute(commandParameters, tenantServiceAccessor);
    }

}
