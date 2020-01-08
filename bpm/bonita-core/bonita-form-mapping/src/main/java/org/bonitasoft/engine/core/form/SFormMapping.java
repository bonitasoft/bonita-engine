/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.PersistentObject;

@Data
@NoArgsConstructor
public class SFormMapping implements PersistentObject {

    public static final String TARGET_INTERNAL = "INTERNAL";
    public static final String TARGET_URL = "URL";
    public static final String TARGET_LEGACY = "LEGACY";
    public static final String TARGET_UNDEFINED = "UNDEFINED";
    public static final String TARGET_NONE = "NONE";

    public static final int TYPE_PROCESS_START = 1;
    public static final int TYPE_PROCESS_OVERVIEW = 2;
    public static final int TYPE_TASK = 3;
    private long id;
    private long tenantId;
    private long processDefinitionId;
    private String task;
    private String target;
    private SPageMapping pageMapping;
    private Integer type = null;
    private long lastUpdateDate;
    private long lastUpdatedBy;

    public SFormMapping(long processDefinitionId, Integer type, String task, String target) {
        this.processDefinitionId = processDefinitionId;
        this.task = task;
        this.type = type;
        this.target = target;
    }

    public String getProcessElementName() {
        switch (FormMappingType.getTypeFromId(this.getType())) {
            case TASK:
                return this.getTask();
            case PROCESS_OVERVIEW:
                return FormMappingType.PROCESS_OVERVIEW.toString();
            case PROCESS_START:
                return FormMappingType.PROCESS_START.toString();
            default:
                return null;
        }
    }
}
