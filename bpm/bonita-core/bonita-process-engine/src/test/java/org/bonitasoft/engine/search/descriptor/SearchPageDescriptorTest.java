/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.search.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.junit.Test;

public class SearchPageDescriptorTest {

    @Test
    public void should_get_all_page_fields() throws Exception {
        //given
        SearchPageDescriptor searchPageDescriptor = new SearchPageDescriptor();

        //when
        final Map<Class<? extends PersistentObject>, Set<String>> allFields = searchPageDescriptor.getAllFields();

        //then
        final Set<String> fields = allFields.get(SPage.class);
        assertThat(fields).contains("name", "displayName");

    }

    @Test
    public void should_get_all_page_keys() throws Exception {
        //given
        SearchPageDescriptor searchPageDescriptor = new SearchPageDescriptor();

        //when
        final Map<String, FieldDescriptor> entityKeys = searchPageDescriptor.getEntityKeys();

        //then
        assertThat(entityKeys).containsKeys("contentType", "processDefinitionId");

    }

}
