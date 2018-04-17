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
package org.bonitasoft.engine.bpm.actor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The descriptor which contains the fields to update an actor.
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0.0
 * @version 6.4.1
 */
public class ActorUpdater implements Serializable {

    private static final long serialVersionUID = -8812266250085063562L;

    /**
     * The fields that can be updated.
     */
    public enum ActorField {
        /**
         * Corresponding to the display name of the actor
         */
        DISPLAY_NAME,
        /**
         * Corresponding to the description of the actor
         */
        DESCRIPTION;
    }

    private final Map<ActorField, Serializable> fields;

    /**
     * The default constructor
     */
    public ActorUpdater() {
        fields = new HashMap<ActorField, Serializable>(ActorField.values().length);
    }

    /**
     * Set the new display name.
     *
     * @param name
     *        The new display name
     */
    public void setDisplayName(final String name) {
        fields.put(ActorField.DISPLAY_NAME, name);
    }

    /**
     * Set the new description.
     *
     * @param description
     *        The new description
     */
    public void setDescription(final String description) {
        fields.put(ActorField.DESCRIPTION, description);
    }

    /**
     * Get the fields to update, and the new value.
     *
     * @return The map containing the pairs (field, new value) to update.
     */
    public Map<ActorField, Serializable> getFields() {
        return fields;
    }

}
