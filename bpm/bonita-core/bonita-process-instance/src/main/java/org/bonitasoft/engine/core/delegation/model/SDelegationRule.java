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
 * Task delegation rule defined by a user (the <i>delegator</i>) granting another user (the <i>delegate</i>)
 * visibility and execution rights on the delegator's human tasks during a bounded delegation period.
 * <p>
 * Delegation is <b>not reassignment</b>: tasks remain assigned to the delegator, and the delegate simply gains the
 * ability to see and execute them through dedicated views. Original ownership and full audit trail are preserved
 * via the existing {@code executedBy} / {@code executedBySubstitute} fields and {@code queryable_log}.
 * <p>
 * A rule applies only to the processes declared in its associated {@link SDelegationRuleProcess} whitelist (matched
 * by process name, covering every deployed version). The rule's lifecycle status — {@code scheduled},
 * {@code active}, or {@code expired} — is derived at query time from {@code startDate} and {@code endDate}; there is
 * no boolean activation flag, and deactivation is performed by deleting the row.
 * <p>
 * A user can hold at most one delegation rule at a time, enforced by a {@code UNIQUE} constraint on
 * {@code delegator_id}. {@code lastUpdatedBy} / {@code lastUpdatedAt} record the last administrative or self-service
 * modification for audit purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "delegation_rule")
public class SDelegationRule implements PersistentObject {

    public static final String ID_KEY = "id";
    public static final String DELEGATOR_ID_KEY = "delegatorId";
    public static final String DELEGATE_ID_KEY = "delegateId";
    public static final String START_DATE_KEY = "startDate";
    public static final String END_DATE_KEY = "endDate";
    public static final String LAST_UPDATED_BY_KEY = "lastUpdatedBy";
    public static final String LAST_UPDATED_AT_KEY = "lastUpdatedAt";

    @Id
    private long id;

    @Column(name = "delegator_id", nullable = false)
    private long delegatorId;

    @Column(name = "delegate_id", nullable = false)
    private long delegateId;

    @Column(name = "start_date", nullable = false)
    private long startDate;

    @Column(name = "end_date", nullable = false)
    private long endDate;

    @Column(name = "last_updated_by", nullable = false)
    private long lastUpdatedBy;

    @Column(name = "last_updated_at", nullable = false)
    private long lastUpdatedAt;

}
