/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.platform;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Informations on the license of a node
 * 
 * @author Matthieu Chaffotte
 */
public interface LicenseInfo extends Serializable {

    /**
     * @return
     *         name of the license owner
     */
    String getLicensee();

    /**
     * @return
     *         date after which the license will expire
     */
    Date getExpirationDate();

    /**
     * @return
     *         name of the edition this license allow to run
     */
    String getEdition();

    /**
     * @return
     *         list of features that are available using this license
     */
    List<String> getFeatures();

    /**
     * @return
     *         the number of CPU cores this license allows
     */
    int getNumberOfCPUCores();

}
