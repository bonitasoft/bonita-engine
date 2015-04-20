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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SContextEntry;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserTaskDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class SUserTaskDefinitionBinding extends SHumanTaskDefinitionBinding {

    private SContractDefinition contract;
    private List<SContextEntry> context;

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLSProcessDefinition.CONTRACT_NODE.equals(name)) {
            contract = (SContractDefinition) value;
        } else if (XMLSProcessDefinition.CONTEXT_NODE.equals(name)) {
            context = (List<SContextEntry>) value;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getObject() {
        final SUserTaskDefinitionImpl userTask = new SUserTaskDefinitionImpl(id, name, actorName);
        fillNode(userTask);
        if (contract != null) {
            userTask.setContract(contract);
        }
        if (context != null) {
            userTask.getContext().addAll(context);
        }
        return userTask;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.USER_TASK_NODE;
    }

}
