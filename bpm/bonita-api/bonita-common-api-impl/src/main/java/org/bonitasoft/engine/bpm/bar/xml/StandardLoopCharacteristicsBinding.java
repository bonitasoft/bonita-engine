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

import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristics;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.io.xml.ElementBinding;

/**
 * @author Matthieu Chaffotte
 */
public class StandardLoopCharacteristicsBinding extends ElementBinding {

    private boolean testBefore;

    private Expression loopCondition;

    private Expression loopMax;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        testBefore = Boolean.valueOf(attributes.get(XMLProcessDefinition.TEST_BEFORE));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.LOOP_CONDITION.equals(name)) {
            loopCondition = (Expression) value;
        }
        if (XMLProcessDefinition.LOOP_MAX.equals(name)) {
            loopMax = (Expression) value;
        }
    }

    @Override
    public Object getObject() {
        if (loopMax == null) {
            return new StandardLoopCharacteristics(loopCondition, testBefore);
        }
        return new StandardLoopCharacteristics(loopCondition, testBefore, loopMax);
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.STANDARD_LOOP_CHARACTERISTICS_NODE;
    }

}
