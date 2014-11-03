/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuUpdateBuilderImpl;

@RunWith(MockitoJUnitRunner.class)
public class IndexUpdaterTest {

    public static final int MAX_RESULTS = 2;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SApplicationMenu menu3;

    @Mock
    private SApplicationMenu menu4;

    @Mock
    private SApplicationMenu menu5;

    private IndexUpdater indexUpdater;

    @Before
    public void setUp() throws Exception {
        indexUpdater = new IndexUpdater(applicationService, MAX_RESULTS);

        given(menu3.getIndex()).willReturn(3);
        given(menu4.getIndex()).willReturn(4);
        given(menu5.getIndex()).willReturn(5);
    }

    @Test
    public void incrementIndexes_should_increment_indexes_of_all_elements_in_the_specified_interval() throws Exception {
        //given
        SApplicationMenuBuilderFactoryImpl appMenuFactory = new SApplicationMenuBuilderFactoryImpl();
        List<OrderByOption> orderBy = Collections.singletonList(new OrderByOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), OrderByType.ASC));
        List<FilterOption> filters = Arrays.asList(new FilterOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), 3, 5), new FilterOption(
                SApplicationMenu.class, appMenuFactory.getParentIdKey(), 1L));

        given(applicationService.searchApplicationMenus(new QueryOptions(0, MAX_RESULTS, orderBy, filters, null))).willReturn(Arrays.asList(menu3, menu4));
        given(applicationService.searchApplicationMenus(new QueryOptions(MAX_RESULTS, MAX_RESULTS, orderBy, filters, null))).willReturn(Arrays.asList(menu5));

        //when
        indexUpdater.incrementIndexes(1L, 3, 5);

        //then

        verify(applicationService).updateApplicationMenu(menu3, getUpdateDescriptorForIndex(4), false);
        verify(applicationService).updateApplicationMenu(menu4, getUpdateDescriptorForIndex(5), false);
        verify(applicationService).updateApplicationMenu(menu5, getUpdateDescriptorForIndex(6), false);
    }

    @Test
    public void incrementIndexes_should_do_nothing_when_from_is_greater_then_to() throws Exception {
        //given
        SApplicationMenuBuilderFactoryImpl appMenuFactory = new SApplicationMenuBuilderFactoryImpl();
        List<OrderByOption> orderBy = Collections.singletonList(new OrderByOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), OrderByType.ASC));
        List<FilterOption> filters = Arrays.asList(new FilterOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), 3, 5), new FilterOption(
                SApplicationMenu.class, appMenuFactory.getParentIdKey(), 1L));

        given(applicationService.searchApplicationMenus(new QueryOptions(0, MAX_RESULTS, orderBy, filters, null))).willReturn(Arrays.asList(menu3, menu4));
        given(applicationService.searchApplicationMenus(new QueryOptions(MAX_RESULTS, MAX_RESULTS, orderBy, filters, null))).willReturn(Arrays.asList(menu5));

        //when
        indexUpdater.incrementIndexes(1L, 4, 3);

        //then
        verify(applicationService, never()).searchApplicationMenus(any(QueryOptions.class));
        verify(applicationService, never()).updateApplicationMenu(any(SApplicationMenu.class), any(EntityUpdateDescriptor.class), anyBoolean());
    }

    private EntityUpdateDescriptor getUpdateDescriptorForIndex(int newIndex) {
        SApplicationMenuUpdateBuilderImpl builder = new SApplicationMenuUpdateBuilderImpl();
        builder.updateIndex(newIndex);
        return builder.done();
    }

    @Test
    public void decrementIndexes_should_decrement_indexes_of_all_elements_in_the_specified_interval() throws Exception {
        //given
        SApplicationMenuBuilderFactoryImpl appMenuFactory = new SApplicationMenuBuilderFactoryImpl();
        List<OrderByOption> orderBy = Collections.singletonList(new OrderByOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), OrderByType.ASC));
        List<FilterOption> filters = Arrays.asList(new FilterOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), 3, 5), new FilterOption(
                SApplicationMenu.class, appMenuFactory.getParentIdKey(), 1L));

        given(applicationService.searchApplicationMenus(new QueryOptions(0, MAX_RESULTS, orderBy, filters, null))).willReturn(Arrays.asList(menu3, menu4));
        given(applicationService.searchApplicationMenus(new QueryOptions(MAX_RESULTS, MAX_RESULTS, orderBy, filters, null))).willReturn(Arrays.asList(menu5));

        //when
        indexUpdater.decrementIndexes(1L, 3, 5);

        //then

        verify(applicationService).updateApplicationMenu(menu3, getUpdateDescriptorForIndex(2), false);
        verify(applicationService).updateApplicationMenu(menu4, getUpdateDescriptorForIndex(3), false);
        verify(applicationService).updateApplicationMenu(menu5, getUpdateDescriptorForIndex(4), false);
    }

}
