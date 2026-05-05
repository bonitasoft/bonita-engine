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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * Process whitelist entry attached to a {@link SDelegationRule}: declares one process (identified by its name) for
 * which the parent rule applies. Each rule must carry at least one entry — a delegation has no effect on a process
 * that is not listed.
 * <p>
 * Entries are unique per rule ({@code UNIQUE (delegation_rule_id, process_name)}) and are removed together with
 * their parent rule ({@code ON DELETE CASCADE}).
 * <p>
 * Process matching is performed by name rather than by definition id so that a single whitelist entry covers every
 * deployed version of the process and remains stable across redeployments.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delegation_rule_process")
public class SDelegationRuleProcess implements PersistentObject {

    public static final String ID_KEY = "id";
    public static final String DELEGATION_RULE_ID_KEY = "delegationRuleId";
    public static final String PROCESS_NAME_KEY = "processName";

    @Id
    private long id;

    @Column(name = "delegation_rule_id", nullable = false)
    private long delegationRuleId;

    @Column(name = "process_name", nullable = false)
    private String processName;

}
