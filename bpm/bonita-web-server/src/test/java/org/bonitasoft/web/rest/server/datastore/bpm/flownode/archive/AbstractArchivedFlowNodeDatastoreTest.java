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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedFlowNodeItem;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class AbstractArchivedFlowNodeDatastoreTest {

    private ArchivedHumanTaskDatastore datastore = new ArchivedHumanTaskDatastore(null, "token");

    @Test
    public void makeSearchOptionCreator_should_converts_TERMINAL_field_to_boolean() {
        final List<SearchFilter> filters = datastore.makeSearchOptionCreator(0, 10, "", "displayName ASC",
                Collections.singletonMap(ArchivedFlowNodeItem.FILTER_IS_TERMINAL, "true")).create().getFilters();
        assertThat(filters.get(0).getValue()).isEqualTo(true);
    }

}
