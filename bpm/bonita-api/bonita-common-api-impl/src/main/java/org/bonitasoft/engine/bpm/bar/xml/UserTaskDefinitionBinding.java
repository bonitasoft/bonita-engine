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
 */
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.List;

import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class UserTaskDefinitionBinding extends HumanTaskDefinitionBinding {

    private ContractDefinition contract;
    private List<ContextEntry> context;

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.USER_TASK_NODE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.CONTRACT_NODE.equals(name)) {
            contract = (ContractDefinition) value;
        }
        if (XMLProcessDefinition.CONTEXT_NODE.equals(name)) {
            context = (List<ContextEntry>) value;
        }
    }

    @Override
    protected void fillNode(final FlowNodeDefinitionImpl flowNode) {
        super.fillNode(flowNode);
        if (contract != null) {
            ((UserTaskDefinitionImpl) flowNode).setContract(contract);
        }
    }

    @Override
    public Object getObject() {
        final UserTaskDefinitionImpl userTaskDefinitionImpl = new UserTaskDefinitionImpl(id, name, actorName);
        fillNode(userTaskDefinitionImpl);
        if(context!= null){
            userTaskDefinitionImpl.getContext().addAll(context);
        }
        return userTaskDefinitionImpl;
    }

}
