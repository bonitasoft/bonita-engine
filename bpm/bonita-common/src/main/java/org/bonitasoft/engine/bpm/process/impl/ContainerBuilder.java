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

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public interface ContainerBuilder extends FlowElementBuilder {

    /**
     * Add a document definition in this container.
     * <p>
     * Must add also the content if the document is not external. This can be done using
     * {@link org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder#addDocumentResource(org.bonitasoft.engine.bpm.bar.BarResource)}
     *
     * @param name
     *      name of the document definition
     * @return
     *      the builder
     */
    DocumentDefinitionBuilder addDocumentDefinition(final String name);

    /**
     * Add a document list definition in this container.
     * <p>
     *
     * @param name
     *        the name of the list
     * @return
     *         the builder to add the optional description and initial value expression
     */
    DocumentListDefinitionBuilder addDocumentListDefinition(final String name);

}
