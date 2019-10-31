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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SubProcessDefinitionBuilder extends FlowElementContainerBuilder implements ContainerBuilder {

    SubProcessDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final SubProcessDefinitionImpl subProcessActivity) {
        super(new FlowElementContainerDefinitionImpl(), processDefinitionBuilder);
        subProcessActivity.setSubProcessContainer(getContainer());
    }

    @Override
    public DocumentDefinitionBuilder addDocumentDefinition(final String name) {
        return new DocumentDefinitionBuilder(getProcessBuilder(), getContainer(), name);
    }

    /**
     * Add a document list definition in this container.
     *
     * @param name the name of the list
     * @return the builder to add the optional description and initial value expression
     */
    @Override
    public DocumentListDefinitionBuilder addDocumentListDefinition(String name) {
        return new DocumentListDefinitionBuilder(getProcessBuilder(), getContainer(), name);
    }

}
