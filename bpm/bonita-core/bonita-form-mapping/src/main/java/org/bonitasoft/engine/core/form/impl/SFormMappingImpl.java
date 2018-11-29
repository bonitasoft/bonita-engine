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
 */
package org.bonitasoft.engine.core.form.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SFormMappingImpl extends PersistentObjectId implements SFormMapping {

    private long processDefinitionId;
    private String task;
    private String target;
    private SPageMapping pageMapping;
    private Integer type = null;
    private long lastUpdateDate;
    private long lastUpdatedBy;

    public SFormMappingImpl(long processDefinitionId, Integer type, String task, String target) {
        this.processDefinitionId = processDefinitionId;
        this.task = task;
        this.type = type;
        this.target = target;
    }
    @Override
    public String getProcessElementName() {
        switch (FormMappingType.getTypeFromId(this.getType())) {
            case TASK: return this.getTask();
            case PROCESS_OVERVIEW: return FormMappingType.PROCESS_OVERVIEW.toString();
            case PROCESS_START: return FormMappingType.PROCESS_START.toString();
            default: return null;
        }
    }
}
