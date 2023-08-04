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
package org.bonitasoft.engine.core.process.instance.model.event.handling;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@Entity
@IdClass(PersistentObjectId.class)
@Table(name = "message_instance")
public class SMessageInstance implements PersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;
    private String messageName;
    private String targetProcess;
    private String targetFlowNode;
    private long processDefinitionId;
    private boolean locked = false;
    private boolean handled = false;
    private String flowNodeName;
    private String correlation1;
    private String correlation2;
    private String correlation3;
    private String correlation4;
    private String correlation5;
    private long creationDate;

    public SMessageInstance(final String messageName, final String targetProcess, final String targetFlowNode,
            final long processDefinitionId,
            final String flowNodeName) {
        this.messageName = messageName;
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        this.processDefinitionId = processDefinitionId;
        this.flowNodeName = flowNodeName;
        this.creationDate = System.currentTimeMillis();
    }

}
