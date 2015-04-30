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

package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.commons.Container;
import org.mockito.ArgumentMatcher;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataContextMatcher extends ArgumentMatcher<BusinessDataContext> {

    private final BusinessDataContext expectedContext;

    public BusinessDataContextMatcher(BusinessDataContext expectedContext) {
        this.expectedContext = expectedContext;
    }

    @Override
    public boolean matches(final Object argument) {
        if (!(argument instanceof BusinessDataContext)) {
            return false;
        }
        BusinessDataContext actualContext = (BusinessDataContext) argument;
        Container expectedContainer = expectedContext.getContainer();
        Container actualContainer = actualContext.getContainer();
        return expectedContext.getName().equals(actualContext.getName()) && expectedContainer.getId() == actualContainer.getId()
                && expectedContainer.getType().equals(actualContainer.getType());
    }
}
