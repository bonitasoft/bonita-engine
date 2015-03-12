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
package org.bonitasoft.engine.bpm.businessdata;

import org.bonitasoft.engine.bpm.DescriptionElement;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Emmanuel Duchastenier
 */
public interface BusinessDataDefinition extends DescriptionElement {

    /**
     * Returns the class name of the Business Data type.
     * 
     * @return the class name of the Business Data type.
     */
    String getClassName();

    /**
     * Returns the default value of the business data, as an Expression to evaluate.
     * 
     * @return the default value of the business data, as an Expression to evaluate.
     */
    Expression getDefaultValueExpression();

    /**
     * Returns if this business data is handled as a List.
     * 
     * @return true if this business data is handled as a List.
     */
    boolean isMultiple();

}
