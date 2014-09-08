/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.platform.command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes how a platform command will be updated.
 *
 * @author Zhang Bole
 */
public class PlatformCommandUpdater implements Serializable {

    private static final long serialVersionUID = 7220637915680451760L;

    public enum PlatformCommandField {
        NAME, DESCRIPTION;
    }

    private final Map<PlatformCommandField, Serializable> fields;

    public PlatformCommandUpdater() {
        fields = new HashMap<PlatformCommandField, Serializable>();
    }

    /**
     * Sets new name content
     *
     * @param name a String defining the new command name
     */
    public void setName(final String name) {
        fields.put(PlatformCommandField.NAME, name);
    }

    /**
     * Sets the new description content
     *
     * @param description a String defining the new description
     */
    public void setDescription(final String description) {
        fields.put(PlatformCommandField.DESCRIPTION, description);
    }

    public Map<PlatformCommandField, Serializable> getFields() {
        return fields;
    }

}
