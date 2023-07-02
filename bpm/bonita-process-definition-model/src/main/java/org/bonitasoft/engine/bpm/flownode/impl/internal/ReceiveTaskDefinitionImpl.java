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

import static org.bonitasoft.engine.operation.OperationBuilder.getNonNullCopy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Julien Molinaro
 * @author Celine Souchet
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class ReceiveTaskDefinitionImpl extends TaskDefinitionImpl implements ReceiveTaskDefinition {

    private static final long serialVersionUID = -5793747387538282891L;

    @XmlElement(name = "catchMessageEventTrigger")
    private final CatchMessageEventTriggerDefinitionImpl trigger;

    public ReceiveTaskDefinitionImpl() {
        super();
        trigger = new CatchMessageEventTriggerDefinitionImpl("default name");
    }

    public ReceiveTaskDefinitionImpl(final String name, final String messageName) {
        super(name);
        trigger = new CatchMessageEventTriggerDefinitionImpl(messageName);
    }

    public ReceiveTaskDefinitionImpl(final long id, final String name,
            final CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition) {
        super(id, name);
        trigger = new CatchMessageEventTriggerDefinitionImpl(catchMessageEventTriggerDefinition);
    }

    public void addCorrelation(final Expression key, final Expression value) {
        trigger.addCorrelation(key, value);
    }

    public void addMessageOperation(final Operation operation) {
        trigger.addOperation(getNonNullCopy(operation));
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
