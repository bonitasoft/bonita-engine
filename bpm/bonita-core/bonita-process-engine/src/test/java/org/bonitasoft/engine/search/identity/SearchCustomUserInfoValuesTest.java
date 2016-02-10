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
package org.bonitasoft.engine.search.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchCustomUserInfoValuesTest {

    @Mock
    private IdentityService service;

    @Mock
    private SearchEntityDescriptor descriptor;

    @Mock
    private SearchOptions options;

    @Test
    public void should_return_a_list_of_CustomUserInfoValues() {
        SearchCustomUserInfoValues search = new SearchCustomUserInfoValues(service, descriptor, options);

        List<CustomUserInfoValue> result = search.convertToClientObjects(Arrays.<SCustomUserInfoValue> asList(
                new DummySCustomUserInfoValue(1L, 3L, 1L, ""),
                new DummySCustomUserInfoValue(2L, 4L, 1L, "")));

        assertThat(result.get(0).getDefinitionId()).isEqualTo(3L);
        assertThat(result.get(1).getDefinitionId()).isEqualTo(4L);
    }
}
