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
package org.bonitasoft.engine.business.application;

import org.bonitasoft.engine.bpm.BaseElement;


/**
 * Represents an {@link Application} menu
 *
 * @author Elias Ricken de Medeiros
 * @since 6.4
 * @see Application
 */
public interface ApplicationMenu extends BaseElement {

    /**
     * Retrieves the {@code ApplicationMenu} display name
     *
     * @return the {@code ApplicationMenu} display name
     */
    String getDisplayName();

    /**
     * Retrieves the identifier of related {@link ApplicationPage}. If the {@code ApplicationMenu} is not related to an {@code ApplicationPage}, this method will return null.
     *
     * @return the identifier of related {@code ApplicationPage} or null if the menu is not related to {@code ApplicationPage}
     * @see ApplicationPage
     */
    Long getApplicationPageId();

    /**
     * Retrieves the identifier of related {@link Application}
     *
     * @return the identifier of related {@code Application}
     * @see Application
     */
    long getApplicationId();

    /**
     * Retrieves the identifier of the parent {@code ApplicationMenu}. If the menu does not have a parent menu, this method will return null.
     *
     * @return the identifier of the parent {@code ApplicationMenu} or null if the menu has no parent.
     */
    Long getParentId();

    /**
     * Retrieves the {@code ApplicationMenu} index
     *
     * @return the {@code ApplicationMenu} index
     */
    int getIndex();

}
