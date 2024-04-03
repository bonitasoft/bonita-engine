/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.model.bpm.cases;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;

public class ArchivedCaseVariable extends ArchivedVariable {

    /**
     * ID of the case this variable belongs to
     */
    @JsonProperty(value = "case_id")
    private String caseId;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public static ArchivedCaseVariable create(ArchivedDataInstance archivedProcessDataInstance) {
        var instance = new ArchivedCaseVariable();
        instance.setName(archivedProcessDataInstance.getName());
        instance.setCaseId(String.valueOf(archivedProcessDataInstance.getContainerId()));
        instance.setDescription(archivedProcessDataInstance.getDescription());
        instance.setType(archivedProcessDataInstance.getClassName());
        Serializable value = archivedProcessDataInstance.getValue();
        instance.setValue(value == null ? null : String.valueOf(value));
        instance.setArchivedDate(archivedProcessDataInstance.getArchiveDate());
        instance.setSourceObjectId(String.valueOf(archivedProcessDataInstance.getSourceObjectId()));
        return instance;
    }

}
