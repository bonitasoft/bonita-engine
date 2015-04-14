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
package org.bonitasoft.engine.api;

import java.io.Serializable;

/**
 * Gives access to some common APIs.
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public interface APIAccessor extends Serializable {

    /**
     * Gives access to the <code>IdentityAPI</code>
     *
     * @return an <code>IdentityAPI</code>
     */
    IdentityAPI getIdentityAPI();

    /**
     * Gives access to the <code>ProcessAPI</code>
     *
     * @return an <code>ProcessAPI</code>
     */
    ProcessAPI getProcessAPI();

    /**
     * Gives access to the <code>CommandAPI</code>
     *
     * @return an <code>CommandAPI</code>
     */
    CommandAPI getCommandAPI();

    /**
     * Gives access to the <code>ProfileAPI</code>
     *
     * @return an <code>ProfileAPI</code>
     */
    ProfileAPI getProfileAPI();

    /**
     * Gives access to the <code>ThemeAPI</code>
     *
     * @return an <code>ThemeAPI</code>
     */
    ThemeAPI getThemeAPI();

    /**
     * Gives access to the <code>PermissionAPI</code>
     *
     * @return an <code>PermissionAPI</code>
     */
    PermissionAPI getPermissionAPI();

    /**
     * Gives access to the <code>PageAPI</code>
     *
     * @return an <code>PageAPI</code>
     */
    PageAPI getCustomPageAPI();

    /**
     * Gives access to the <code>ApplicationAPI</code>
     *
     * @return an <code>ApplicationAPI</code>
     */
    ApplicationAPI getLivingApplicationAPI();

    ProcessConfigurationAPI getProcessConfigurationAPI();
    
    /**
     * Gives access to the <code>BusinessDataAPI</code>
     *
     * @return an <code>BusinessDataAPI</code>
     */
    BusinessDataAPI getBusinessDataAPI();

}
