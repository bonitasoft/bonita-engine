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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.operation.pojo.Travel;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetBusinessDataByIdCommandTest {

    private static final String PARAMETER_CHILDNAME = "lines";

    private static final long PARAMETER_IDENTIFIER = 1983L;

    private static final String PARAMETER_CLASS_NAME = Travel.class.getName();

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";

    private GetBusinessDataByIdCommand command;

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private BusinessDataService businessDataService;

    private Map<String, Serializable> parameters;

    @Before
    public void setUp() throws Exception {
        command = new GetBusinessDataByIdCommand();
        parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, PARAMETER_IDENTIFIER);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, PARAMETER_CLASS_NAME);
        parameters.put(BusinessDataCommandField.BUSINESS_DATA_URI_PATTERN, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
        when(tenantServiceAccessor.getBusinessDataService()).thenReturn(businessDataService);
    }

    @Test
    public void executeCommandWithEntity() throws Exception {
        //when
        command.execute(parameters, tenantServiceAccessor);

        //then
        verify(businessDataService).getJsonEntity(PARAMETER_CLASS_NAME, PARAMETER_IDENTIFIER, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test(expected = SCommandExecutionException.class)
    public void executeCommandWithEntityShloudThrowException() throws Exception {
        //given
        doThrow(SBusinessDataNotFoundException.class).when(businessDataService).getJsonEntity(PARAMETER_CLASS_NAME, PARAMETER_IDENTIFIER,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //when then exception
        command.execute(parameters, tenantServiceAccessor);
    }

    @Test(expected = SCommandExecutionException.class)
    public void executeCommandWithChildEntityShouldThrowException() throws Exception {
        //given
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, PARAMETER_CHILDNAME);
        doThrow(SBusinessDataRepositoryException.class).when(businessDataService).getJsonChildEntity(PARAMETER_CLASS_NAME, PARAMETER_IDENTIFIER,
                PARAMETER_CHILDNAME,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //when then exception
        command.execute(parameters, tenantServiceAccessor);
    }

    @Test
    public void executeCommandWithEmptyChildName() throws Exception {
        //given
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "");
        doThrow(SBusinessDataRepositoryException.class).when(businessDataService).getJsonChildEntity(PARAMETER_CLASS_NAME, PARAMETER_IDENTIFIER,
                PARAMETER_CHILDNAME,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //when
        command.execute(parameters, tenantServiceAccessor);

        //then
        verify(businessDataService).getJsonEntity(PARAMETER_CLASS_NAME, PARAMETER_IDENTIFIER, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test
    public void executeCommandWithChildEntity() throws Exception {
        //given
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, PARAMETER_CHILDNAME);

        //when
        command.execute(parameters, tenantServiceAccessor);

        //then
        verify(businessDataService).getJsonChildEntity(PARAMETER_CLASS_NAME, PARAMETER_IDENTIFIER, PARAMETER_CHILDNAME, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

}
