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
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "incomings", "outgoings", "connectors", "description", "displayDescription", "displayName",
        "displayDescriptionAfterCompletion",
        "defaultTransition", "dataDefinitions", "businessDataDefinitions", "operations", "loopCharacteristics",
        "boundaryEventDefinitions",
        "userFilterDefinition", "contract", "context", "expectedDuration" })
public class UserTaskDefinitionImpl extends HumanTaskDefinitionImpl implements UserTaskDefinition {

    private static final long serialVersionUID = -8168685139931497082L;
    @XmlElement(type = ContractDefinitionImpl.class)
    private ContractDefinition contract;
    @XmlElementWrapper(name = "context")
    @XmlElement(name = "contextEntry", type = ContextEntryImpl.class)
    private List<ContextEntry> context = new ArrayList<>();

    public UserTaskDefinitionImpl() {
    }

    public UserTaskDefinitionImpl(final String name, final String actorName) {
        super(name, actorName);
    }

    public UserTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name, actorName);
    }

    public void setContract(final ContractDefinition contract) {
        this.contract = contract;
    }

    @Override
    public ContractDefinition getContract() {
        return contract;
    }

    @Override
    public List<ContextEntry> getContext() {
        return context;
    }

    public void addContextEntry(ContextEntry contextEntry) {
        context.add(contextEntry);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        UserTaskDefinitionImpl that = (UserTaskDefinitionImpl) o;
        return Objects.equals(contract, that.contract) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contract, context);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("contract", contract)
                .append("context", context)
                .toString();
    }
}
