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
package org.bonitasoft.engine.business.application.impl;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class IndexManager {

    private IndexUpdater updater;
    private MenuIndexValidator validator;

    public IndexManager(IndexUpdater updater, MenuIndexValidator validator) {
        this.updater = updater;
        this.validator = validator;
    }

    public void organizeIndexesOnDelete(MenuIndex deletedMenuIndex) throws SBonitaReadException, SObjectModificationException {
        updater.decrementIndexes(deletedMenuIndex.getParentId(), deletedMenuIndex.getValue() + 1, deletedMenuIndex.getLastUsedIndex());
    }

    public void organizeIndexesOnUpdate(MenuIndex oldIndex, MenuIndex newIndex) throws SBonitaReadException, SObjectModificationException {
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
