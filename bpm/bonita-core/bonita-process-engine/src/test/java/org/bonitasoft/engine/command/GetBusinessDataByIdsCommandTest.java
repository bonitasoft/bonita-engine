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

import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.operation.pojo.Travel;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetBusinessDataByIdsCommandTest {

    private static final List<Long> identifers;

    static {
        identifers = new ArrayList<>();
        identifers.add(1983L);
        identifers.add(1990L);
    }

    private static final String PARAMETER_CLASS_NAME = Travel.class.getName();

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";

    private GetBusinessDataByIdsCommand command;

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private BusinessDataService businessDataService;

    private Map<String, Serializable> parameters;

    @Before
    public void setUp() throws Exception {
        command = new GetBusinessDataByIdsCommand();
        parameters = new HashMap<>();
        parameters.put(GetBusinessDataByIdsCommand.BUSINESS_DATA_IDS, (Serializable) identifers);
        parameters.put(GetBusinessDataByIdsCommand.ENTITY_CLASS_NAME, PARAMETER_CLASS_NAME);
        parameters.put(BusinessDataCommandField.BUSINESS_DATA_URI_PATTERN, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
        when(tenantServiceAccessor.getBusinessDataService()).thenReturn(businessDataService);
    }

    @Test
    public void executeCommandWithEntities() throws Exception {
        //when
        command.execute(parameters, tenantServiceAccessor);

        //then
        verify(businessDataService).getJsonEntities(PARAMETER_CLASS_NAME, identifers, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);
    }

    @Test(expected = SCommandExecutionException.class)
    public void executeCommandWithEntitiesShloudThrowException() throws Exception {
        //given
        doThrow(SBusinessDataRepositoryException.class).when(businessDataService).getJsonEntities(PARAMETER_CLASS_NAME, identifers,
                PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        //when then exception
        command.execute(parameters, tenantServiceAccessor);
    }

}
