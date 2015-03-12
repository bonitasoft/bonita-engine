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
package org.bonitasoft.engine.bpm.category;

import java.util.Date;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Category forms part of the ProcessDefinition.
 * 
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface Category extends BonitaObject {

    /**
     * @return The identifier of the category
     */
    long getId();

    /**
     * @return The name of the category
     */
    String getName();

    /**
     * @return The description of the category
     */
    String getDescription();

    /**
     * @return The identifier of the user that created the category
     */
    long getCreator();

    /**
     * @return The date of creation of the category
     */
    Date getCreationDate();

    /**
     * @return The last date when the category is updated
     */
    Date getLastUpdate();

}
