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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexManagerTest {

    @Mock
    private IndexUpdater indexUpdater;

    @Mock
    private MenuIndexValidator validator;

    @InjectMocks
    private IndexManager indexManager;

    @Test
    public void organizeIndexesOnDelete_should_call_decrement_indexes() throws Exception {
        //given
        MenuIndex menuIndex = new MenuIndex(1L, 2, 9);

        //when
        indexManager.organizeIndexesOnDelete(menuIndex);

        //then
        verify(indexUpdater).decrementIndexes(menuIndex.getParentId(), menuIndex.getValue() + 1, menuIndex.getLastUsedIndex());
        verify(indexUpdater, never()).incrementIndexes(anyLong(), anyInt(), anyInt());
    }

    @Test
    public void organizeIndexesOnUpdate_should_call_increment_index_when_move_up_and_parent_doesnt_change() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(1L, 6, 15);
        MenuIndex newIndex = new MenuIndex(1L, 2, 15);

        //when
        indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);

        //then
        verify(indexUpdater).incrementIndexes(oldIndex.getParentId(), newIndex.getValue(), oldIndex.getValue() - 1);
        verify(indexUpdater, never()).decrementIndexes(anyLong(), anyInt(), anyInt());
    }

    @Test
    public void organizeIndexesOnUpdate_should_call_decrement_index_when_move_down_and_parent_doesnt_change() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(1L, 1, 15);
        MenuIndex newIndex = new MenuIndex(1L, 5, 15);

        //when
        indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);

        //then
        verify(indexUpdater).decrementIndexes(1L, oldIndex.getValue() + 1, newIndex.getValue());
        verify(indexUpdater, never()).incrementIndexes(anyLong(), anyInt(), anyInt());
    }

    @Test
    public void organizeIndexesOnUpdate_should_call_increment_on_new_parent_and_decrement_on_old_parent_when_parent_changes() throws Exception {
        MenuIndex oldIndex = new MenuIndex(1L, 2, 15);
        MenuIndex newIndex = new MenuIndex(2L, 6, 9);

        //when
        indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);

        //then
        verify(indexUpdater).decrementIndexes(oldIndex.getParentId(), oldIndex.getValue() + 1, oldIndex.getLastUsedIndex());
        verify(indexUpdater).incrementIndexes(newIndex.getParentId(), newIndex.getValue(), newIndex.getLastUsedIndex());
    }

    @Test (expected = SObjectModificationException.class)
    public void organizeIndexesOnUpdate_should_throws_SObjectModificationException_when_new_menuIndex_is_invalid() throws Exception {
        //given
        MenuIndex oldIndex = new MenuIndex(1L, 1, 15);
        MenuIndex newIndex = new MenuIndex(1L, 5, 4);
        given(validator.validate(oldIndex, newIndex)).willReturn(Arrays.asList("invalid"));

        //when
        indexManager.organizeIndexesOnUpdate(oldIndex, newIndex);

        //then exception
    }

}
