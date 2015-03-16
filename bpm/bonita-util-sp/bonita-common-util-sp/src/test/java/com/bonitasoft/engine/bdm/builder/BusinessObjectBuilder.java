/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.builder;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.Index;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.QueryParameter;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.Field;

public class BusinessObjectBuilder {

    private BusinessObject businessObject = new BusinessObject();

    public BusinessObjectBuilder(final String qualifiedName) {
        businessObject = new BusinessObject();
        businessObject.setQualifiedName(qualifiedName);
    }

    public static BusinessObjectBuilder aBO(final String qualifiedName) {
        return new BusinessObjectBuilder(qualifiedName);
    }

    public BusinessObject build() {
        return businessObject;
    }

    public BusinessObjectBuilder withField(final Field field) {
        businessObject.addField(field);
        return this;
    }

    public BusinessObjectBuilder withUniqueConstraint(final UniqueConstraint uniqueConstraint) {
        businessObject.addUniqueConstraint(uniqueConstraint);
        return this;
    }

    public BusinessObjectBuilder withIndex(final Index index) {
        businessObject.addIndex(index);
        return this;
    }

    public BusinessObjectBuilder withDescription(final String description) {
        businessObject.setDescription(description);
        return this;
    }

    public BusinessObjectBuilder withQuery(final Query query) {
        final Query addQuery = businessObject.addQuery(query.getName(), query.getContent(), query.getReturnType());
        for (final QueryParameter qP : query.getQueryParameters()) {
            addQuery.addQueryParameter(qP.getName(), qP.getClassName());
        }
        return this;
    }

}
