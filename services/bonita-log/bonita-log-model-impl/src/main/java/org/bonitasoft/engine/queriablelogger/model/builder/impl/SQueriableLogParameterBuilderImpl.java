/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.queriablelogger.model.builder.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.persistence.model.impl.BlobValueImpl;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogParameter;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogParameterBuilder;
import org.bonitasoft.engine.queriablelogger.model.impl.SQueriableLogParameterImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SQueriableLogParameterBuilderImpl implements SQueriableLogParameterBuilder {

    private SQueriableLogParameterImpl entity;

    private List<String> problems;

    @Override
    public SQueriableLogParameterBuilder createNewInstance(final String name, final String valueType) {
        entity = new SQueriableLogParameterImpl();
        entity.setName(name);
        entity.setValueType(valueType);
        return this;
    }

    @Override
    public SQueriableLogParameterBuilder stringValue(final String stringValue) {
        entity.setStringValue(stringValue);
        return this;
    }

    @Override
    public SQueriableLogParameterBuilder blobValue(final Serializable blobValue) {
        final BlobValueImpl blob = new BlobValueImpl();
        blob.setValue(blobValue);
        entity.setBlobValue(blob);
        return this;
    }

    @Override
    public SQueriableLogParameter done() {
        problems = new ArrayList<String>();
        checkMandatoryFields();
        if (problems.size() > 0) {
            throw new MissingMandatoryFieldsException("Some mandatory fields were not set: " + problems);
        }
        return entity;
    }

    private void checkMandatoryFields() {
        if (entity.getName() == null) {
            problems.add("name");
        }
        if (entity.getValueType() == null) {
            problems.add("valueType");
        }
    }

}
