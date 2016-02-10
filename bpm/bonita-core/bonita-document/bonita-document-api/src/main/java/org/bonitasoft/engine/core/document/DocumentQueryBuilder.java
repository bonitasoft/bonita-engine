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
package org.bonitasoft.engine.core.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Baptiste Mesta, Emmanuel Duchastenier
 */
public class DocumentQueryBuilder implements Serializable {

    private static final long serialVersionUID = 5788988731741295555L;

    private final List<Object> query;

    private boolean searchAllVersions = false;

    public DocumentQueryBuilder() {
        query = new ArrayList<Object>();
    }

    public DocumentCriterion criterion(final DocumentField index) {
        DocumentCriterion criterion = new DocumentCriterion(index, this);
        query.add(criterion);
        return criterion;
    }

    public DocumentQueryBuilder leftParenthesis() {
        query.add("(");
        return this;
    }

    public DocumentQueryBuilder rightParenthesis() {
        query.add(")");
        return this;
    }

    public DocumentQueryBuilder or() {
        query.add("OR");
        return this;
    }

    public DocumentQueryBuilder and() {
        query.add("AND");
        return this;
    }

    public List<Object> getQuery() {
        return query;
    }

    public DocumentQueryBuilder allVersion() {
        this.searchAllVersions = true;
        return this;
    }

    public DocumentQueryBuilder latestVersion() {
        this.searchAllVersions = false;
        return this;
    }

    /**
     * @return
     */
    public boolean isSearchAllVersions() {
        return searchAllVersions;
    }

}
