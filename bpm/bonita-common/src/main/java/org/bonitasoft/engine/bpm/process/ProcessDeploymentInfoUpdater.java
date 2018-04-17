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
package org.bonitasoft.engine.bpm.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Updater object to update {@link ProcessDeploymentInfo}.
 *
 * @author Emmanuel Duchastenier
 * @author Zhang Bole
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessDeploymentInfoUpdater implements Serializable {

    private static final long serialVersionUID = 8000868488852784706L;

    /**
     * Fields that can be updated on a {@link ProcessDeploymentInfo}.
     * 
     * @author Emmanuel Duchastenier
     */
    public enum ProcessDeploymentInfoField {
        /**
         * Display name of the process
         */
        DISPLAY_NAME,

        /**
         * Display description of the process
         */
        DISPLAY_DESCRIPTION,

        /**
         * Path of the icon of the process
         */
        ICONPATH
    }

    private final Map<ProcessDeploymentInfoField, Serializable> fields;

    /**
     * Construct the updater object with no field to update.
     */
    public ProcessDeploymentInfoUpdater() {
        fields = new HashMap<ProcessDeploymentInfoField, Serializable>(ProcessDeploymentInfoField.values().length);
    }

    /**
     * Set the new display name of the process definition.
     * 
     * @param name
     *        The new display name of the process definition.
     * @since 6.3.5
     */
    public void setDisplayName(final String name) {
        fields.put(ProcessDeploymentInfoField.DISPLAY_NAME, name);
    }

    /**
     * Set the new display description of the process definition.
     * 
     * @param description
     *        The new display description of the process definition.
     * @since 6.3.5
     */
    public void setDisplayDescription(final String description) {
        fields.put(ProcessDeploymentInfoField.DISPLAY_DESCRIPTION, description);
    }

    /**
     * Set the new icon path of the process definition.
     * 
     * @param iconPath
     *        The new icon path of the process definition.
     * @since 6.3.5
     */
    public void setIconPath(final String iconPath) {
        fields.put(ProcessDeploymentInfoField.ICONPATH, iconPath);
    }

    /**
     * Get the map of the fields to update with their new value.
     * 
     * @return The map of the fields to update with their new value.
     * @since 6.3.5
     */
    public Map<ProcessDeploymentInfoField, Serializable> getFields() {
        return fields;
    }

}
