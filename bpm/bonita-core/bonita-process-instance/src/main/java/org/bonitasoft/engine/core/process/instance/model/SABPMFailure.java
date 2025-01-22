/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.ArchivedPlatformPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.hibernate.annotations.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "arch_bpm_failure")
public class SABPMFailure implements ArchivedPlatformPersistentObject {

    @Id
    private long id;
    private long processDefinitionId;
    private long processInstanceId;
    private long rootProcessInstanceId;
    private long flowNodeInstanceId;
    private String scope;
    private String context;
    private String errorMessage;
    @Type(type = "materialized_clob")
    private String stackTrace;
    private long failureDate;
    private long archiveDate;
    private long sourceObjectId;

    public SABPMFailure(final SBPMFailure bpmFailure) {
        this.sourceObjectId = bpmFailure.getId();
        this.processDefinitionId = bpmFailure.getProcessDefinitionId();
        this.processInstanceId = bpmFailure.getProcessInstanceId();
        this.rootProcessInstanceId = bpmFailure.getRootProcessInstanceId();
        this.flowNodeInstanceId = bpmFailure.getFlowNodeInstanceId();
        this.scope = bpmFailure.getScope();
        this.context = bpmFailure.getContext();
        this.errorMessage = bpmFailure.getErrorMessage();
        this.stackTrace = bpmFailure.getStackTrace();
        this.failureDate = bpmFailure.getFailureDate();
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SBPMFailure.class;
    }

}
