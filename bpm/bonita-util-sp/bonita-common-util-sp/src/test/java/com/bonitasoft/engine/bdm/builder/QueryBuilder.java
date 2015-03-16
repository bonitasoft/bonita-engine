/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.builder;

import com.bonitasoft.engine.bdm.model.Query;

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
