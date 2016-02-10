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
package org.bonitasoft.engine.api.impl.transaction.expression.bdm.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.expression.EntityMerger;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


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
