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
package org.bonitasoft.engine.core.process.definition.model;

import org.bonitasoft.engine.expression.model.SExpression;

/**
 * A document list is a named element that define a list of document on a process
 * It contains 0 or more document and have a name to reference it in the process instance
 * It is initialized when the process start using the {@link #getExpression()} expression
 *
 * @author Baptiste Mesta
 * @since 6.4.0
 */
public interface SDocumentListDefinition extends SNamedElement {

    /**
     * @return the description of the document list
     */
    String getDescription();

    /**
     * The expression that will be evaluated when we initialize the document list
     * 
     * @return the initial value expression
     */
    SExpression getExpression();

}
