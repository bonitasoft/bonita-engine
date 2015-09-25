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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.impl.OperationImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CatchMessageEventTriggerDefinitionImpl extends MessageEventTriggerDefinitionImpl implements CatchMessageEventTriggerDefinition {

    private static final long serialVersionUID = -8667216649689173514L;
    @XmlElement(name = "operation", type = OperationImpl.class)
    private final List<Operation> operations;

    public CatchMessageEventTriggerDefinitionImpl(final String messageName) {
        super(messageName);
        operations = new ArrayList<>(1);
    }

    public CatchMessageEventTriggerDefinitionImpl() {
        super();
        operations = new ArrayList<>(1);
    }
    public CatchMessageEventTriggerDefinitionImpl(final CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition) {
        super(catchMessageEventTriggerDefinition);
        operations = catchMessageEventTriggerDefinition.getOperations();
    }

    @Override
    public List<Operation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    public void addOperation(final Operation operation) {
        operations.add(operation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CatchMessageEventTriggerDefinitionImpl that = (CatchMessageEventTriggerDefinitionImpl) o;
        return Objects.equals(operations, that.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operations);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
