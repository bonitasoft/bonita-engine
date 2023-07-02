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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowErrorEventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class EndEventDefinitionImpl extends ThrowEventDefinitionImpl implements EndEventDefinition {

    private static final long serialVersionUID = -6726824751097930154L;

    @XmlElement(type = ThrowErrorEventTriggerDefinitionImpl.class, name = "throwErrorEventTrigger")
    private final List<ThrowErrorEventTriggerDefinition> errorEventTriggerDefinitions = new ArrayList<>(1);
    @Getter
    @Setter
    @XmlElement(type = TerminateEventTriggerDefinitionImpl.class, name = "terminateEventTrigger")
    private TerminateEventTriggerDefinition terminateEventTriggerDefinition;

    public EndEventDefinitionImpl(final String name) {
        super(name);
    }

    public EndEventDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    @Override
    public List<ThrowErrorEventTriggerDefinition> getErrorEventTriggerDefinitions() {
        return Collections.unmodifiableList(errorEventTriggerDefinitions);
    }

    public void addErrorEventTriggerDefinition(final ThrowErrorEventTriggerDefinition errorEventTrigger) {
        errorEventTriggerDefinitions.add(errorEventTrigger);
        addEventTrigger(errorEventTrigger);
    }
}
