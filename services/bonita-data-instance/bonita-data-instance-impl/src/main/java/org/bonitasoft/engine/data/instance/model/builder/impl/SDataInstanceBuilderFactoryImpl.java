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
package org.bonitasoft.engine.data.instance.model.builder.impl;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.STextDataDefinition;
import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.impl.SBlobDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SBooleanDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SDateDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SDoubleDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SFloatDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SIntegerDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SLongDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SLongTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SXMLDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SXMLObjectDataInstanceImpl;
import org.bonitasoft.engine.expression.model.SExpression;

import java.util.Date;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SDataInstanceBuilderFactoryImpl implements SDataInstanceBuilderFactory {

    @Override
    public SDataInstanceBuilder createNewInstance(final SDataDefinition dataDefinition) {
        final SExpression expression = dataDefinition.getDefaultValueExpression();
        final String className = dataDefinition.getClassName();
        SDataInstanceImpl dataInstanceImpl = null;
        if (dataDefinition instanceof STextDataDefinition) {
            dataInstanceImpl = getTextDataInstance((STextDataDefinition) dataDefinition, expression);
        } else if (dataDefinition instanceof SXMLDataDefinition) {
            dataInstanceImpl = new SXMLDataInstanceImpl((SXMLDataDefinition) dataDefinition);
        } else {
            if (Integer.class.getName().equals(className)) {
                dataInstanceImpl = new SIntegerDataInstanceImpl(dataDefinition);
            } else if (Long.class.getName().equals(className)) {
                dataInstanceImpl = new SLongDataInstanceImpl(dataDefinition);
            } else if (String.class.getName().equals(className)) {
                dataInstanceImpl = new SShortTextDataInstanceImpl(dataDefinition);
            } else if (Boolean.class.getName().equals(className)) {
                dataInstanceImpl = new SBooleanDataInstanceImpl(dataDefinition);
            } else if (Double.class.getName().equals(className)) {
                dataInstanceImpl = new SDoubleDataInstanceImpl(dataDefinition);
            } else if (Float.class.getName().equals(className)) {
                dataInstanceImpl = new SFloatDataInstanceImpl(dataDefinition);
            } else if (byte[].class.getName().equals(className)) {
                dataInstanceImpl = new SBlobDataInstanceImpl(dataDefinition);
            } else if (Date.class.getName().equals(className)) {
                dataInstanceImpl = new SDateDataInstanceImpl(dataDefinition);
            } else {
                dataInstanceImpl = new SXMLObjectDataInstanceImpl(dataDefinition);
            }
        }
        return new SDataInstanceBuilderImpl(dataInstanceImpl);
    }

    private SDataInstanceImpl getTextDataInstance(final STextDataDefinition dataDefinition, final SExpression expression) {
        SDataInstanceImpl dataInstance = null;
        if (dataDefinition.isLongText()) {
            dataInstance = new SLongTextDataInstanceImpl(dataDefinition);
        } else {
            dataInstance = new SShortTextDataInstanceImpl(dataDefinition);
        }
        dataInstance.setValue(null);
        return dataInstance;
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getLabelKey() {
        return "label";
    }

    @Override
    public String getDescriptionKey() {
        return "description";
    }

    @Override
    public String getTransientDataKey() {
        return "transientData";
    }

    @Override
    public String getclassNameKey() {
        return "classname";
    }

    @Override
    public String getValueKey() {
        return "value";
    }

    @Override
    public String getContainerIdKey() {
        return "containerId";
    }

    @Override
    public String getContainerTypeKey() {
        return "containerType";
    }

    @Override
    public String getArchiveDateKey() {
        return "archiveDate";
    }

}
