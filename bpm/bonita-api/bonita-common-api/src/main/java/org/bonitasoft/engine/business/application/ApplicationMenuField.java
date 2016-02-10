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


    /**
     * Contains fields used by {@link ApplicationMenuCreator} and {@link ApplicationMenuUpdater}
     * @author Elias Ricken de Medeiros
     * @since 6.4
     */
    public enum ApplicationMenuField {

        /**
         * References the {@link ApplicationMenu} display name
         *
         * @see ApplicationMenu
         */
        DISPLAY_NAME,

        /**
         * References the identifier of {@link Application} related to the {@link ApplicationMenu}
         *
         * @see ApplicationMenu
         * @see Application
         */
        APPLICATION_ID,

        /**
         * References the identifier of {@link ApplicationPage} related to the {@link ApplicationMenu}
         *
         * @see ApplicationMenu
         * @see ApplicationPage
         */
        APPLICATION_PAGE_ID,

        /**
         * References the identifier of parent {@link ApplicationMenu}
         *
         * @see ApplicationMenu
         */
        PARENT_ID,

        /**
         * References the {@link ApplicationMenu} index
         *
         * @see ApplicationMenu
         */
        INDEX;

    }