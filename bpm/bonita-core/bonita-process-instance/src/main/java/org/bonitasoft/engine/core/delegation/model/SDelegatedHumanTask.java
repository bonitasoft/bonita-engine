/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.delegation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * Search-result row joining a human task with its delegator's active delegation rule.
 * Returned by {@link DelegationRuleService#searchDelegatedTasks}.
 * <p>
 * Wraps the underlying {@link SHumanTaskInstance} entity so the public API can reuse the
 * existing {@code ModelConvertor.toHumanTaskInstance} conversion path; the additional
 * fields carry the delegation context plus root-process info that has to be joined from
 * other tables.
 * <p>
 * Implements {@link PersistentObject} so it can flow through the standard
 * {@code AbstractSearchEntity} pipeline; it is not itself a JPA entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SDelegatedHumanTask implements PersistentObject {

    private SHumanTaskInstance humanTaskInstance;
    private SProcessDefinitionDeployInfo rootProcessDeployInfo;
    private long delegatorId;
    private long delegateId;
    private long delegationStart;
    private long delegationEnd;

    @Override
    public long getId() {
        return humanTaskInstance != null ? humanTaskInstance.getId() : 0L;
    }

    @Override
    public void setId(final long id) {
        // No-op: the id is derived from the wrapped SHumanTaskInstance. PersistentObject is a
        // community framework interface and may be invoked reflectively by hydration / recorder
        // paths we do not control here; throwing would turn a benign no-op call into a runtime
        // failure.
    }
}
