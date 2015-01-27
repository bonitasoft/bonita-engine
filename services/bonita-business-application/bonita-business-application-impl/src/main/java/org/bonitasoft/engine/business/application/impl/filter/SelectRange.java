/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.impl.filter;

/**
 * @author Elias Ricken de Medeiros
 */
public class SelectRange {
    private int startIndex;
    private int maxResults;

    public SelectRange(int startIndex, int maxResults) {
        this.startIndex = startIndex;
        this.maxResults = maxResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectRange)) return false;

        SelectRange that = (SelectRange) o;

        if (maxResults != that.maxResults) return false;
        if (startIndex != that.startIndex) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + maxResults;
        return result;
    }
}
