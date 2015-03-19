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
package org.bonitasoft.engine.bdm.builder;

import org.bonitasoft.engine.bdm.model.Query;

/**
 * @author Romain Bioteau
 */
public class QueryBuilder {

    private final Query query = new Query();

    public static QueryBuilder aQuery() {
        return new QueryBuilder();
    }

    public QueryBuilder withName(final String name) {
        query.setName(name);
        return this;
    }

    public QueryBuilder withContent(final String content) {
        query.setContent(content);
        return this;
    }

    public QueryBuilder withQueryParameter(final String name, final String className) {
        query.addQueryParameter(name, className);
        return this;
    }

    public QueryBuilder withReturnType(final String className) {
        query.setReturnType(className);
        return this;
    }

    public Query build() {
        return query;
    }
}
