/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;

import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationBuilderFactoryImpl;

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
