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
package org.bonitasoft.engine.bpm.category;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class CategoryUpdater implements Serializable {

    private static final long serialVersionUID = 8964190598176457557L;

    public enum CategoryField {
        NAME, DESCRIPTION;
    }

    private final Map<CategoryField, Serializable> fields;

    public CategoryUpdater() {
        fields = new HashMap<CategoryField, Serializable>(5);
    }

    public CategoryUpdater setName(final String name) {
        fields.put(CategoryField.NAME, name);
        return this;
    }

    public CategoryUpdater setDescription(final String description) {
        fields.put(CategoryField.DESCRIPTION, description);
        return this;
    }

    public Map<CategoryField, Serializable> getFields() {
        return fields;
    }

}
