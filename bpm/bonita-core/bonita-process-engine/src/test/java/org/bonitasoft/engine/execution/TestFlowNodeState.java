/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

public class TestFlowNodeState implements FlowNodeState {

    private final SStateCategory stateCategory;
    private final boolean stable;
    private final boolean terminal;
    private final int id;
    private final boolean shouldBeExecuted;

    private TestFlowNodeState(int id, SStateCategory stateCategory, boolean stable, boolean terminal,
            boolean shouldBeExecuted) {
        this.id = id;
        this.stateCategory = stateCategory;
        this.stable = stable;
        this.terminal = terminal;
        this.shouldBeExecuted = shouldBeExecuted;
    }

    public static TestFlowNodeState terminalState(int id, SStateCategory stateCategory) {
        return new TestFlowNodeState(id, stateCategory, false, true, true);
    }

    public static TestFlowNodeState stableState(int id, SStateCategory stateCategory) {
        return new TestFlowNodeState(id, stateCategory, true, false, true);
    }

    public static TestFlowNodeState normalState(int id, SStateCategory stateCategory) {
        return new TestFlowNodeState(id, stateCategory, false, false, true);
    }

    public static TestFlowNodeState skippedState(int id, SStateCategory stateCategory) {
        return new TestFlowNodeState(id, stateCategory, false, false, false);
    }

    @Override
    public boolean shouldExecuteState(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance) {
        return shouldBeExecuted;
    }

    @Override
    public boolean mustAddSystemComment(SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(SFlowNodeInstance flowNodeInstance) {
        return null;
    }

    @Override
    public StateCode execute(SProcessDefinition processDefinition, SFlowNodeInstance instance)
            throws SActivityStateExecutionException {
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return "test-state-" + id;
    }

    @Override
    public boolean isStable() {
        return stable;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public SStateCategory getStateCategory() {
        return stateCategory;
    }
}
