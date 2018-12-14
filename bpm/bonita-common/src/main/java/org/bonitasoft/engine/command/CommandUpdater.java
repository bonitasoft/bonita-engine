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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Updater for <code>Commands</code> <br>
 *
 * @author Matthieu Chaffotte
 * @see org.bonitasoft.engine.api.CommandAPI#update(long, CommandUpdater)
 * @see org.bonitasoft.engine.api.CommandAPI#update(String, CommandUpdater)
 */
public class CommandUpdater implements Serializable {

    private static final long serialVersionUID = 1326464578602375090L;

    public enum CommandField {
        NAME, DESCRIPTION
    }

    private final Map<CommandField, Serializable> fields;

    public CommandUpdater() {
        fields = new HashMap<CommandField, Serializable>(2);
    }

    public void setName(final String name) {
        fields.put(CommandField.NAME, name);
    }

    public void setDescription(final String description) {
        fields.put(CommandField.DESCRIPTION, description);
    }

    public Map<CommandField, Serializable> getFields() {
        return fields;
    }

}
