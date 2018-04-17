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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class CategoryUpdater implements Serializable {

    private static final long serialVersionUID = 8964190598176457557L;

    /**
     * The fields that can be updated.
     */
    public enum CategoryField {
        /**
         * The name of the field corresponding to the name of the category
         */
        NAME,

        /**
         * The name of the field corresponding to the description of the category
         */
        DESCRIPTION;
    }

    private final Map<CategoryField, Serializable> fields;

    /**
     * The default constructor
     */
    public CategoryUpdater() {
        fields = new HashMap<CategoryField, Serializable>(5);
    }

    /**
     * Set the new name
     * 
     * @param name
     *            The new name
     * @return The CategoryUpdater containing the new name
     */
    public CategoryUpdater setName(final String name) {
        fields.put(CategoryField.NAME, name);
        return this;
    }

    /**
     * Set the new description
     * 
     * @param description
     *            The new description
     * @return The CategoryUpdater containing the new description
     */
    public CategoryUpdater setDescription(final String description) {
        fields.put(CategoryField.DESCRIPTION, description);
        return this;
    }

    /**
     * Get the fields to update, and the new value
     * 
     * @return The map containing the pairs (field, new value) to update.
     */
    public Map<CategoryField, Serializable> getFields() {
        return fields;
    }

}
