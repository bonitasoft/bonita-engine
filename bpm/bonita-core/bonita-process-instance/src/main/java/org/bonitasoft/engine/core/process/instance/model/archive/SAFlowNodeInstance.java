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
package org.bonitasoft.engine.core.process.instance.model.archive;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@Entity
@Table(name = "arch_flownode_instance")
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "kind")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class SAFlowNodeInstance implements ArchivedPersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;
    private long archiveDate;
    private long sourceObjectId;
    private String name;
    private long rootContainerId;
    private long parentContainerId;
    private boolean aborting;
    private long logicalGroup1;
    private long logicalGroup2;
    private long logicalGroup3;
    private long logicalGroup4;
    private int stateId;
    private String stateName;
    private boolean terminal;
    private boolean stable;
    private long reachedStateDate;
    private long lastUpdateDate;
    private String displayDescription;
    private String displayName;
    private String description;
    private long executedBy;
    private long executedBySubstitute;
    //The field is mapped on the object and is also used for the discriminator, that is why it is not insertable and updatable
    @Column(insertable = false, updatable = false)
    private String kind;
    @Column(name = "flownodeDefinitionId")
    private long flowNodeDefinitionId;

    public SAFlowNodeInstance(final SFlowNodeInstance flowNodeInstance) {
        sourceObjectId = flowNodeInstance.getId();
        name = flowNodeInstance.getName();
        rootContainerId = flowNodeInstance.getRootContainerId();
        parentContainerId = flowNodeInstance.getParentContainerId();
        logicalGroup1 = flowNodeInstance.getLogicalGroup(0);
        logicalGroup2 = flowNodeInstance.getLogicalGroup(1);
        logicalGroup3 = flowNodeInstance.getLogicalGroup(2);
        logicalGroup4 = flowNodeInstance.getLogicalGroup(3);
        stateId = flowNodeInstance.getStateId();
        stateName = flowNodeInstance.getStateName();
        reachedStateDate = flowNodeInstance.getReachedStateDate();
        lastUpdateDate = flowNodeInstance.getLastUpdateDate();
        terminal = flowNodeInstance.isTerminal();
        stable = flowNodeInstance.isStable();
        displayName = flowNodeInstance.getDisplayName();
        displayDescription = flowNodeInstance.getDisplayDescription();
        description = flowNodeInstance.getDescription();
        executedBy = flowNodeInstance.getExecutedBy();
        executedBySubstitute = flowNodeInstance.getExecutedBySubstitute();
        flowNodeDefinitionId = flowNodeInstance.getFlowNodeDefinitionId();
    }

    public long getProcessDefinitionId() {
        return logicalGroup1;
    }

    public long getRootProcessInstanceId() {
        return logicalGroup2;
    }

    public long getParentActivityInstanceId() {
        return logicalGroup3;
    }

    public long getParentProcessInstanceId() {
        return logicalGroup4;
    }

    public void setLogicalGroup(final int index, final long id) {
        switch (index) {
            case 0:
                logicalGroup1 = id;
                break;
            case 1:
                logicalGroup2 = id;
                break;
            case 2:
                logicalGroup3 = id;
                break;
            case 3:
                logicalGroup4 = id;
                break;
            default:
                throw new IndexOutOfBoundsException("Index out of range for setLogicalGroup: " + index);
        }
    }

    public long getLogicalGroup(final int index) {
        switch (index) {
            case 0:
                return logicalGroup1;
            case 1:
                return logicalGroup2;
            case 2:
                return logicalGroup3;
            case 3:
                return logicalGroup4;
            default:
                throw new IllegalArgumentException("Invalid index: must be 0, 1, 2 or 3");
        }
    }

    abstract public SFlowNodeType getType();

}
