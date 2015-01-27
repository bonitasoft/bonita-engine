/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.impl.filter;


import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.impl.filter.ApplicationRelatedMenusFilterBuilder;
import org.bonitasoft.engine.business.application.impl.filter.SelectRange;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;

public class ApplicationRelatedMenusFilterBuilderTest {

    public static final int START_INDEX = 0;
    public static final int MAX_RESULTS = 10;

    @Test
    public void buildQueryOptions_should_filter_on_applicationId() throws Exception {
        //given
        long applicationId = 1;
        SApplicationMenuBuilderFactoryImpl factory = new SApplicationMenuBuilderFactoryImpl();
        ApplicationRelatedMenusFilterBuilder builder = new ApplicationRelatedMenusFilterBuilder(new SelectRange(START_INDEX, MAX_RESULTS), applicationId);

        //when
        QueryOptions options = builder.buildQueryOptions();

        //then
        assertThat(options).isNotNull();
        assertThat(options.getFromIndex()).isEqualTo(START_INDEX);
        assertThat(options.getNumberOfResults()).isEqualTo(MAX_RESULTS);
        assertThat(options.getOrderByOptions()).containsExactly(new OrderByOption(SApplicationMenu.class, factory.getIdKey(), OrderByType.ASC));
        FilterOption appFilter = new FilterOption(SApplicationMenu.class, factory.getApplicationIdKey(), applicationId);
        FilterOption parentFilter = new FilterOption(SApplicationMenu.class, factory.getParentIdKey(), null);
        assertThat(options.getFilters()).containsExactly(appFilter, parentFilter);
    }

}
