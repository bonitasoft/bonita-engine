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
package org.bonitasoft.engine.bpm.actor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class ActorUpdater implements Serializable {

    private static final long serialVersionUID = -8812266250085063562L;

    public enum ActorField {
        DISPLAY_NAME, DESCRIPTION;
    }

    private final Map<ActorField, Serializable> fields;

    public ActorUpdater() {
        fields = new HashMap<ActorField, Serializable>(ActorField.values().length);
    }

    public void setDisplayName(final String name) {
        fields.put(ActorField.DISPLAY_NAME, name);
    }

    public void setDescription(final String description) {
        fields.put(ActorField.DESCRIPTION, description);
    }

    public Map<ActorField, Serializable> getFields() {
        return fields;
    }

}
