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
package org.bonitasoft.engine.supervisor.mapping.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "processsupervisor")
@IdClass(PersistentObjectId.class)
public class SProcessSupervisor implements PersistentObject {

    public static final String ID_KEY = "id";
    public static final String USER_ID_KEY = "userId";
    public static final String GROUP_ID_KEY = "groupId";
    public static final String ROLE_ID_KEY = "roleId";
    public static final String PROCESS_DEF_ID_KEY = "processDefId";
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private long processDefId;
    @Builder.Default
    @Column
    private long userId = -1;
    @Builder.Default
    @Column
    private long groupId = -1;
    @Builder.Default
    @Column
    private long roleId = -1;

    public SProcessSupervisor(final long processDefId) {
        this.processDefId = processDefId;
    }

}
