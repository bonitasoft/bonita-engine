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
package org.bonitasoft.engine.core.process.definition.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContextEntry;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserTaskDefinitionImpl extends SHumanTaskDefinitionImpl implements SUserTaskDefinition {

    private static final long serialVersionUID = 9039679250456947450L;

    private SContractDefinition contract;
    private final List<SContextEntry> context = new ArrayList<>();

    public SUserTaskDefinitionImpl(final UserTaskDefinition userTaskDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(userTaskDefinition, transitionsMap);
        final ContractDefinition contract = userTaskDefinition.getContract();
        if (contract != null) {
            setContract(new SContractDefinitionImpl(contract));
        }
        for (ContextEntry contextEntry : userTaskDefinition.getContext()) {
            context.add(new SContextEntryImpl(contextEntry.getKey(), ServerModelConvertor.convertExpression(contextEntry.getExpression())));
        }
    }

    public SUserTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name, actorName);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.USER_TASK;
    }

    @Override
    public SContractDefinition getContract() {
        if (contract == null) {
            return new SContractDefinitionImpl();
        }
        return contract;
    }

    @Override
    public List<SContextEntry> getContext() {
        return context;
    }

    public void setContract(final SContractDefinition contract) {
        this.contract = contract;
    }

}
