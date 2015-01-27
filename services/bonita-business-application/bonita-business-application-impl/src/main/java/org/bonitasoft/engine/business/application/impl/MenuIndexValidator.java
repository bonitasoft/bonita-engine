/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Elias Ricken de Medeiros
 */
public class MenuIndexValidator {


    public List<String> validate(MenuIndex oldIndex, MenuIndex newIndex) {
        final List<String> problems = new ArrayList<String>(1);
        int lastValidIndex = getLastValidIndex(oldIndex, newIndex);
        if (newIndex.getValue() < 1 || newIndex.getValue() > lastValidIndex) {
            problems.add(new StringBuilder().append("Invalid menu index: ").append(newIndex.getValue())
                    .append(". It must be between 1 and the number of menu in your application having the same parent. The last valid index for parent ")
                    .append(newIndex.getParentId()).append(" is ").append(lastValidIndex).toString());
        }
        return problems;
    }

    private int getLastValidIndex(MenuIndex oldIndex, MenuIndex newIndex) {
        int lastValidIndex = newIndex.getLastUsedIndex();
        if(oldIndex.getParentId() != newIndex.getParentId()) {
            // a new element will be added in this parent
            lastValidIndex = newIndex.getLastUsedIndex() + 1;
        }
        return lastValidIndex;
    }

}
