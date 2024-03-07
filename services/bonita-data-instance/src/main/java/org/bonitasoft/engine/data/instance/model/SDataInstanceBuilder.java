/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.data.instance.model;

import java.io.Serializable;
import java.util.Date;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.STextDataDefinition;
import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;

/**
 * @author Zhao Na
 */
public class SDataInstanceBuilder {

    private final SDataInstance dataInstance;

    public static SDataInstanceBuilder createNewInstance(final SDataDefinition dataDefinition) {
        final String className = dataDefinition.getClassName();
        SDataInstance dataInstance;
        if (dataDefinition instanceof STextDataDefinition) {
            dataInstance = getTextDataInstance((STextDataDefinition) dataDefinition);
        } else if (dataDefinition instanceof SXMLDataDefinition) {
            dataInstance = new SXMLDataInstance((SXMLDataDefinition) dataDefinition);
        } else {
            if (Integer.class.getName().equals(className)) {
                dataInstance = new SIntegerDataInstance(dataDefinition);
            } else if (Long.class.getName().equals(className)) {
                dataInstance = new SLongDataInstance(dataDefinition);
            } else if (String.class.getName().equals(className)) {
                dataInstance = new SShortTextDataInstance(dataDefinition);
            } else if (Boolean.class.getName().equals(className)) {
                dataInstance = new SBooleanDataInstance(dataDefinition);
            } else if (Double.class.getName().equals(className)) {
                dataInstance = new SDoubleDataInstance(dataDefinition);
            } else if (Float.class.getName().equals(className)) {
                dataInstance = new SFloatDataInstance(dataDefinition);
            } else if (byte[].class.getName().equals(className)) {
                dataInstance = new SBlobDataInstance(dataDefinition);
            } else if (Date.class.getName().equals(className)) {
                dataInstance = new SDateDataInstance(dataDefinition);
            } else {
                dataInstance = new SXMLObjectDataInstance(dataDefinition);
            }
        }
        return new SDataInstanceBuilder(dataInstance);
    }

    public static SDataInstance createNewInstance(final SDataDefinition dataDefinition, long containerId,
            String containerType, Serializable value) throws SDataInstanceNotWellFormedException {
        return createNewInstance(dataDefinition).setContainerId(containerId).setContainerType(containerType)
                .setValue(value).done();
    }

    private static SDataInstance getTextDataInstance(final STextDataDefinition dataDefinition) {
        SDataInstance dataInstance;
        if (dataDefinition.isLongText()) {
            dataInstance = new SLongTextDataInstance(dataDefinition);
        } else {
            dataInstance = new SShortTextDataInstance(dataDefinition);
        }
        dataInstance.setValue(null);
        return dataInstance;
    }

    private SDataInstanceBuilder(final SDataInstance dataInstance) {
        super();
        this.dataInstance = dataInstance;
    }

    public SDataInstanceBuilder setValue(final Serializable value) {
        dataInstance.setValue(value);
        return this;
    }

    public SDataInstanceBuilder setContainerId(final long containerId) {
        dataInstance.setContainerId(containerId);
        return this;
    }

    public SDataInstanceBuilder setContainerType(final String containerType) {
        dataInstance.setContainerType(containerType);
        return this;
    }

    public SDataInstance done() throws SDataInstanceNotWellFormedException {
        dataInstance.validate();
        return dataInstance;
    }

}
