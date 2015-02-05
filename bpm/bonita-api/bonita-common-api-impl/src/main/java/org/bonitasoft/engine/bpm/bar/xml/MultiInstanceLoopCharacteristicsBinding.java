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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.io.xml.ElementBinding;

/**
 * @author Baptiste Mesta
 */
public class MultiInstanceLoopCharacteristicsBinding extends ElementBinding {

    private Boolean isSequential;

    private String dataInputItemRef;

    private String dataOutputItemRef;

    private String loopDataInput;

    private String loopDataOutput;

    private Expression loopCardinality;

    private Expression completionCondition;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        isSequential = Boolean.valueOf(attributes.get(XMLProcessDefinition.MULTI_INSTANCE_IS_SEQUENTIAL));
        dataInputItemRef = attributes.get(XMLProcessDefinition.MULTI_INSTANCE_DATA_INPUT_ITEM_REF);
        dataOutputItemRef = attributes.get(XMLProcessDefinition.MULTI_INSTANCE_DATA_OUTPUT_ITEM_REF);
        loopDataInput = attributes.get(XMLProcessDefinition.MULTI_INSTANCE_LOOP_DATA_INPUT);
        loopDataOutput = attributes.get(XMLProcessDefinition.MULTI_INSTANCE_LOOP_DATA_OUTPUT);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.MULTI_INSTANCE_LOOP_CARDINALITY.equals(name)) {
            loopCardinality = (Expression) value;
        }
        if (XMLProcessDefinition.MULTI_INSTANCE_COMPLETION_CONDITION.equals(name)) {
            completionCondition = (Expression) value;
        }
    }

    @Override
    public Object getObject() {
        MultiInstanceLoopCharacteristics loopCharacteristics;
        if (loopCardinality != null) {
            loopCharacteristics = new MultiInstanceLoopCharacteristics(isSequential, loopCardinality);
        } else {
            loopCharacteristics = new MultiInstanceLoopCharacteristics(isSequential, loopDataInput);
        }
        loopCharacteristics.setCompletionCondition(completionCondition);
        loopCharacteristics.setDataInputItemRef(dataInputItemRef);
        loopCharacteristics.setDataOutputItemRef(dataOutputItemRef);
        loopCharacteristics.setLoopDataOutputRef(loopDataOutput);
        return loopCharacteristics;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.MULTI_INSTANCE_LOOP_CHARACTERISTICS_NODE;
    }

}
