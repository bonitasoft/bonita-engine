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

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.QueryParameter;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;

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
