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
package org.bonitasoft.web.rest.server.api.bdm;

import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataFindByIdsResourceTest extends RestletTest {

    @Mock
    private CommandAPI commandAPI;

    @Override
    protected ServerResource configureResource() {
        return new BusinessDataFindByIdsResource(commandAPI);
    }

    @Test
    public void findByIds_should_return_the_list_of_business_objects() throws Exception {
        List<Long> ids = new ArrayList<>();
        ids.add(1983L);
        ids.add(547862L);
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", "org.bonitasoft.pojo.Employee");
        parameters.put("businessDataIds", (Serializable) ids);
        parameters.put("businessDataURIPattern", BusinessDataFieldValue.URI_PATTERN);
        when(commandAPI.execute("getBusinessDataByIds", parameters)).thenReturn("[{\"id\":1983},{\"id\":547862}]");

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/findByIds?ids=1983,547862")
                .get();

        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response).hasJsonEntityEqualTo("[{\"id\":1983},{\"id\":547862}]");
    }

}
