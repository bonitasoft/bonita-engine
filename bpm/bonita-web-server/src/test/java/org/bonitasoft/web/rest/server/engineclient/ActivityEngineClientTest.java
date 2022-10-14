/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.engineclient;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ActivityEngineClientTest extends APITestWithMock {

    @Mock
    private ProcessAPI processAPI;

    private ActivityEngineClient activityEngineClient;

    @Before
    public void initializeClient() {
        initMocks(this);
        activityEngineClient = new ActivityEngineClient(processAPI);
    }

    @Test(expected = APIException.class)
    public void getDataInstance_throw_exception_if_data_is_not_found() throws Exception {
        when(processAPI.getActivityDataInstance(anyString(), anyLong()))
                .thenThrow(new DataNotFoundException(new NullPointerException()));

        activityEngineClient.getDataInstance("aName", 1L);
    }

}
