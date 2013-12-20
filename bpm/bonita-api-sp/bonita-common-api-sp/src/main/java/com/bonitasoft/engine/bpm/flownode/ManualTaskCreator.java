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
package com.bonitasoft.engine.bpm.flownode;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.TaskPriority;

/**
 * @author Elias Ricken de Medeiros
 */
public class ManualTaskCreator implements Serializable {

    private static final long serialVersionUID = 3817894732943459008L;

    public enum ManualTaskField {
        PARENT_TASK_ID, TASK_NAME, DISPLAY_NAME, ASSIGN_TO, DESCRIPTION, DUE_DATE, PRIORITY;
    }

    private final Map<ManualTaskField, Serializable> fields;

    public ManualTaskCreator(final long parentTaskId, final String taskName) {
        fields = new HashMap<ManualTaskField, Serializable>(7);
        fields.put(ManualTaskField.PARENT_TASK_ID, parentTaskId);
        fields.put(ManualTaskField.TASK_NAME, taskName);

    }

    public ManualTaskCreator setDisplayName(final String displayName) {
        fields.put(ManualTaskField.DISPLAY_NAME, displayName);
        return this;
    }

    public ManualTaskCreator setAssignTo(final long assignTo) {
        fields.put(ManualTaskField.ASSIGN_TO, assignTo);
        return this;
    }

    public ManualTaskCreator setDescription(final String description) {
        fields.put(ManualTaskField.DESCRIPTION, description);
        return this;
    }

    public ManualTaskCreator setDueDate(final Date dueDate) {
        fields.put(ManualTaskField.DUE_DATE, dueDate);
        return this;
    }

    public ManualTaskCreator setPriority(final TaskPriority taskPriority) {
        fields.put(ManualTaskField.PRIORITY, taskPriority);
        return this;
    }

    public Map<ManualTaskField, Serializable> getFields() {
        return fields;
    }

}
