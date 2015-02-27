/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.expression.bdm.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.transaction.expression.EntityMerger;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityMergerTest {

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private Entity testEntity;

    private EntityMerger entityMerger;

    @Before
    public void setUp() {
        entityMerger = new EntityMerger(bdrService);
    }

    @Test
    public void merge_an_single_entity_should_not_call_merge_on_bdrService() {
        entityMerger.merge(testEntity);
        verify(bdrService, never()).merge(testEntity);
    }

    @Test
    public void merge_a_collection_of_entity_should_not_call_merge_on_bdrService_for_each_entity() {
        final List<Entity> listOfEntities = new ArrayList<Entity>();
        listOfEntities.add(testEntity);
        listOfEntities.add(testEntity);
        listOfEntities.add(testEntity);
        entityMerger.merge((Serializable) listOfEntities);
        verify(bdrService, never()).merge(testEntity);
    }

    @Test
    public void merge_a_simple_serializable_should_not_call_merge_on_bdrService() {
        final String hello = "Hello";
        assertThat(entityMerger.merge("Hello")).isEqualTo(hello);
        verifyZeroInteractions(bdrService);
    }

    @Test
    public void merge_a_collection_of_simple_serializable_should_not_call_merge_on_bdrService() {
        final List<String> listOfEntities = new ArrayList<String>();
        listOfEntities.add("Hello");
        listOfEntities.add("Goodbye");
        listOfEntities.add("Have a nice day");
        entityMerger.merge((Serializable) listOfEntities);
        verifyZeroInteractions(bdrService);
    }

}
