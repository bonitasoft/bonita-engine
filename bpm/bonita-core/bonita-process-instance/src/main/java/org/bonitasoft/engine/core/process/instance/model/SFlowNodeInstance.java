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
package org.bonitasoft.engine.core.process.instance.model;

import java.util.Date;

import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Feng Hui
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "flownode_instance")
@IdClass(PersistentObjectId.class)
@DiscriminatorColumn(name = "kind")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class SFlowNodeInstance implements PersistentObject {

    @Id
    private long id;
    @Id
    private long tenantId;
    private long flowNodeDefinitionId;
    private long rootContainerId;
    private long parentContainerId;
    private String name;
    private String displayName;
    private String displayDescription;
    private int stateId;
    private String stateName;
    @Column(name = "prev_state_id")
    private int previousStateId;
    private boolean terminal;
    private boolean stable;
    @Enumerated(EnumType.STRING)
    private SStateCategory stateCategory = SStateCategory.NORMAL;
    private long reachedStateDate;
    private long lastUpdateDate;
    //process definition id
    private long logicalGroup1;
    //root process instance id
    private long logicalGroup2;
    //parent activity instance id
    private long logicalGroup3;
    //parent process instance id
    private long logicalGroup4;
    private int tokenCount = 0;
    private String description;
    @Column(name = "loop_counter")
    protected int loopCounter;
    /**
     * id of the user who originally executed the flow node
     */
    @Column
    private long executedBy;
    /**
     * id of the user (delegate) who executed the flow node for the original executer
     */
    @Column
    private long executedBySubstitute;
    @Column(name = "state_executing")
    private boolean stateExecuting;

    public SFlowNodeInstance(final String name, final long flowNodeDefinitionId, final long rootContainerId,
            final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        this.name = name;
        this.rootContainerId = rootContainerId;
        this.parentContainerId = parentContainerId;
        this.logicalGroup1 = logicalGroup1;
        this.logicalGroup2 = logicalGroup2;
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        long now = new Date().getTime();
        lastUpdateDate = now;
        reachedStateDate = now;
    }

    public long getProcessDefinitionId() {
        return logicalGroup1;
    }

    /**
     * @return the root process instance is the top level process containing this element
     */
    public long getRootProcessInstanceId() {
        return logicalGroup2;
    }

    /**
     * @return
     *         the id of the activity instance containing this element or 0 if this element is not contained in an
     *         activity
     */
    public long getParentActivityInstanceId() {
        return logicalGroup3;
    }

    /**
     * @return
     *         the id of the process instance containing this element
     */
    public long getParentProcessInstanceId() {
        return logicalGroup4;
    }

    /**
     * @return
     *         the type of the element that contains this element
     */
    public SFlowElementsContainerType getParentContainerType() {
        return getParentActivityInstanceId() <= 0 ? SFlowElementsContainerType.PROCESS
                : SFlowElementsContainerType.FLOWNODE;
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
                throw new IllegalArgumentException("Invalid index: the index must be 0, 1, 2 or 3");
        }
    }

    public boolean isAborting() {
        return SStateCategory.ABORTING.equals(stateCategory);
    }

    public boolean isCanceling() {
        return SStateCategory.CANCELLING.equals(stateCategory);
    }

    public void setLogicalGroup(final int index, final long value) {
        switch (index) {
            case 0:
                logicalGroup1 = value;
                break;
            case 1:
                logicalGroup2 = value;
                break;
            case 2:
                logicalGroup3 = value;
                break;
            case 3:
                logicalGroup4 = value;
                break;
            default:
                throw new IllegalArgumentException("Invalid index: the index must be 0, 1, 2 or 3");
        }
    }

    public SFlowElementsContainerType getContainerType() {
        return SFlowElementsContainerType.FLOWNODE;
    }

    /**
     * @return true if the execution must continues automatically on abort or cancel the parent process instance
     */
    public boolean mustExecuteOnAbortOrCancelProcess() {
        return isStable() && !isTerminal();
    }

    public abstract SFlowNodeType getType();
}
