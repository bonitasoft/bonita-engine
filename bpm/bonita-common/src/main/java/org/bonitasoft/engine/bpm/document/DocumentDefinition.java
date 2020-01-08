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
package org.bonitasoft.engine.bpm.document;

import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.expression.Expression;

/**
 * The definition of a document attached to a process definition
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface DocumentDefinition extends NamedElement, Visitable {

    /**
     * @return The URL for an external document
     */
    String getUrl();

    /**
     * @return The file reference in the process resources
     */
    String getFile();

    /**
     * @return The mime type of the document's content.
     */
    String getContentMimeType();

    /**
     * @return The description of the document
     */
    String getDescription();

    /**
     * @return The name of the file of the document
     */
    String getFileName();

    /**
     * @return the initial value expression
     */
    Expression getInitialValue();

}
