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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import static org.bonitasoft.web.rest.model.builder.bpm.process.CategoryItemBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class CategoryDatastoreTest extends APITestWithMock {

    @Mock
    private ProcessAPI processAPI;

    private CategoryDatastore categoryDatastore;

    @Before
    public void initializeMocks() {
        initMocks(this);

        categoryDatastore = spy(new CategoryDatastore(null));

        doReturn(this.processAPI).when(this.categoryDatastore).getProcessAPI();
    }

    @SuppressWarnings("unchecked")
    @Test(expected = APIForbiddenException.class)
    public void addingTwiceSameCategoryIsForbidden() throws Exception {
        when(processAPI.createCategory(anyString(), anyString()))
                .thenThrow(new AlreadyExistsException("category already exists"));

        categoryDatastore.add(aCategoryItem().build());
    }

}
