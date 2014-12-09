/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.document.impl.DocumentListDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class DocumentListDefinitionBuilder extends FlowElementContainerBuilder {

    private final DocumentListDefinitionImpl documentListDefinitionImpl;

    public DocumentListDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final String name) {
        super(container, processDefinitionBuilder);
        documentListDefinitionImpl = new DocumentListDefinitionImpl(name);
        container.addDocumentListDefinition(documentListDefinitionImpl);
    }

    /**
     * Sets description on this document
     * 
     * @param description description
     * @return
     *         the builder
     */
    public DocumentListDefinitionBuilder addDescription(final String description) {
        documentListDefinitionImpl.setDescription(description);
        return this;
    }

    /**
     * @param expression
     *        the expression that initialize the document list
     *        <p>
     *        the expression must return a List of {@link org.bonitasoft.engine.bpm.document.DocumentValue} </p>
     * @return
     *         the builder
     */
    public DocumentListDefinitionBuilder addInitialValue(final Expression expression) {
        documentListDefinitionImpl.setExpression(expression);
        return this;
    }

}
