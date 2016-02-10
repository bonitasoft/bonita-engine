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
package org.bonitasoft.engine.business.application.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationBuilderFactoryImpl;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;

public class ApplicationsWithIdsFilterBuilderTest {

    @Test
    public void buildQueryOptions_should_filter_on_applicationIds() throws Exception {
        //given
        SApplicationBuilderFactoryImpl factory = new SApplicationBuilderFactoryImpl();
        ApplicationsWithIdsFilterBuilder builder = new ApplicationsWithIdsFilterBuilder(4L, 7L, 10L);

        //when
        QueryOptions queryOptions = builder.buildQueryOptions();

        //then
        assertThat(queryOptions).isNotNull();
        assertThat(queryOptions.getFromIndex()).isEqualTo(0);
        assertThat(queryOptions.getNumberOfResults()).isEqualTo(3);
        assertThat(queryOptions.getOrderByOptions()).containsExactly(new OrderByOption(SApplication.class, factory.getIdKey(), OrderByType.ASC));
        FilterOption filterOption = new FilterOption(SApplication.class, factory.getIdKey());
        filterOption.in(4L, 7L, 10L);
        assertThat(queryOptions.getFilters()).containsExactly(filterOption);
    }

}
