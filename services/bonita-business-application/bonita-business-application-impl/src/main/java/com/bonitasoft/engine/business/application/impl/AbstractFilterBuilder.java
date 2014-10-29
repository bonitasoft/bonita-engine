/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class AbstractFilterBuilder implements FilterBuilder {
    private int startIndex;
    private int maxResults;

    public AbstractFilterBuilder(int startIndex, int maxResults) {
        this.startIndex = startIndex;
        this.maxResults = maxResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getMaxResults() {
        return maxResults;
    }

}
