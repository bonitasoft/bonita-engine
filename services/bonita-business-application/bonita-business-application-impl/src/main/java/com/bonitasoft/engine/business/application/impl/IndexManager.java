/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Elias Ricken de Medeiros
 */
public class IndexManager {

    private IndexUpdater updater;
    private MenuIndexValidator validator;
    private MenuIndexConvertor convertor;

    public IndexManager(IndexUpdater updater, MenuIndexValidator validator) {
        this.updater = updater;
        this.validator = validator;
    }

    public void organizeIndexesOnDelete(MenuIndex deletedMenuIndex) throws SBonitaSearchException, SObjectModificationException {
        updater.decrementIndexes(deletedMenuIndex.getParentId(), deletedMenuIndex.getValue() + 1, deletedMenuIndex.getLastUsedIndex());
    }

    public void organizeIndexesOnUpdate(MenuIndex oldIndex, MenuIndex newIndex) throws SBonitaSearchException, SObjectModificationException {
        validateNewIndex(oldIndex, newIndex);
        if(oldIndex.getParentId() == newIndex.getParentId()) {
            if (newIndex.getValue() < oldIndex.getValue()) {
                updater.incrementIndexes(oldIndex.getParentId(), newIndex.getValue(), oldIndex.getValue() - 1);
            } else {
                updater.decrementIndexes(oldIndex.getParentId(), oldIndex.getValue() + 1, newIndex.getValue());
            }
        } else {
            updater.incrementIndexes(newIndex.getParentId(), newIndex.getValue(), newIndex.getLastUsedIndex());
            updater.decrementIndexes(oldIndex.getParentId(), oldIndex.getValue() + 1, oldIndex.getLastUsedIndex());
        }
    }

    private void validateNewIndex(MenuIndex oldIndex, MenuIndex newIndex) throws SObjectModificationException {
        List<String> validationProblems = validator.validate(oldIndex, newIndex);
        if (!validationProblems.isEmpty()) {
            throw new SObjectModificationException(validationProblems.toString());
        }
    }

}
