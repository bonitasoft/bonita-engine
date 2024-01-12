/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.process.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Updater object to update the string indexes.
 *
 * @author Danila Mazour
 * @author Emmanuel Duchastenier
 * @since 7.12.0
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance
 */
public class ProcessInstanceUpdater implements Serializable {

    public enum ProcessInstanceField {
        /**
         * Corresponding to the first search key
         */
        STRING_INDEX_1,
        /**
         * Corresponding to the second search key
         */
        STRING_INDEX_2,
        /**
         * Corresponding to the third search key
         */
        STRING_INDEX_3,
        /**
         * Corresponding to the fourth search key
         */
        STRING_INDEX_4,
        /**
         * Corresponding to the fifth search key
         */
        STRING_INDEX_5
    }

    private final Map<ProcessInstanceField, Serializable> fields;

    /**
     * Default Constructor with no field to update.
     */
    public ProcessInstanceUpdater() {
        fields = new HashMap<>();
    }

    /**
     * Set the new value for the first search key, also known as string index.
     *
     * @param stringIndex
     *        The new value for the first search key.
     */
    public void setStringIndex1(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_1, stringIndex);
    }

    /**
     * Set the new value for the second search key, also known as string index.
     *
     * @param stringIndex
     *        The new value for the second search key.
     */
    public void setStringIndex2(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_2, stringIndex);
    }

    /**
     * Set the new value for the third search key, also known as string index.
     *
     * @param stringIndex
     *        The new value for the third search key.
     */
    public void setStringIndex3(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_3, stringIndex);
    }

    /**
     * Set the new value for the fourth search key, also known as string index.
     *
     * @param stringIndex
     *        The new value for the fourth search keyx.
     */
    public void setStringIndex4(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_4, stringIndex);
    }

    /**
     * Set the new value for the fifth search key, also known as string index.
     *
     * @param stringIndex
     *        The new value for the fifth search key.
     */
    public void setStringIndex5(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_5, stringIndex);
    }

    /**
     * Get the map of the fields to update and their new value.
     *
     * @return The map of the fields to update and their new value.
     */
    public Map<ProcessInstanceField, Serializable> getFields() {
        return fields;
    }

}
