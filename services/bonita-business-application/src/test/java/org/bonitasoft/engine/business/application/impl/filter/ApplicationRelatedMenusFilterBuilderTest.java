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
package org.bonitasoft.engine.business.application.impl.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;

public class ApplicationRelatedMenusFilterBuilderTest {

    public static final int START_INDEX = 0;
    public static final int MAX_RESULTS = 10;

    @Test
    public void buildQueryOptions_should_filter_on_applicationId() {
        //given
        long applicationId = 1;
        ApplicationRelatedMenusFilterBuilder builder = new ApplicationRelatedMenusFilterBuilder(
                new SelectRange(START_INDEX, MAX_RESULTS), applicationId);

        //when
        QueryOptions options = builder.buildQueryOptions();

        //then
        assertThat(options).isNotNull();
        assertThat(options.getFromIndex()).isEqualTo(START_INDEX);
        assertThat(options.getNumberOfResults()).isEqualTo(MAX_RESULTS);
        assertThat(options.getOrderByOptions())
                .containsExactly(new OrderByOption(SApplicationMenu.class, SApplicationMenu.ID, OrderByType.ASC));
        FilterOption appFilter = new FilterOption(SApplicationMenu.class, SApplicationMenu.APPLICAITON_ID,
                applicationId);
        FilterOption parentFilter = new FilterOption(SApplicationMenu.class, SApplicationMenu.PARENT_ID, null);
        assertThat(options.getFilters()).containsExactly(appFilter, parentFilter);
    }

}
