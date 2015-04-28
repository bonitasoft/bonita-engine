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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.SXMLParseException;

/**
 * @author Matthieu Chaffotte
 */
public class SContractDefinitionBinding extends ElementBinding {

    private final SContractDefinitionImpl contract;

    public SContractDefinitionBinding() {
        contract = new SContractDefinitionImpl();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) throws SXMLParseException {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws SXMLParseException {
    }

    @Override
    public void setChildObject(final String name, final Object value) throws SXMLParseException {
        if (XMLSProcessDefinition.CONTRACT_INPUT_NODE.equals(name)) {
            contract.addInput((SInputDefinition) value);
        }
        else if (XMLSProcessDefinition.CONTRACT_CONSTRAINT_NODE.equals(name)) {
            contract.addConstraint((SConstraintDefinition) value);
        }
    }

    @Override
    public Object getObject() {
        return contract;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CONTRACT_NODE;
    }

}
