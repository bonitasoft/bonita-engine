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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.io.xml.ElementBinding;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class MessageEventTriggerDefinitionBinding extends ElementBinding {

    private String messageName;

    private final List<CorrelationDefinition> correlations = new ArrayList<CorrelationDefinition>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        messageName = attributes.get(XMLProcessDefinition.NAME);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.CORRELATION_NODE.equals(name)) {
            correlations.add((CorrelationDefinition) value);
        }
    }

    protected void fillNode(final MessageEventTriggerDefinitionImpl messageEventTrigger) {
        for (final CorrelationDefinition correlation : correlations) {
            messageEventTrigger.addCorrelation(correlation.getKey(), correlation.getValue());
        }
    }

    protected String getMessageName() {
        return messageName;
    }

}
