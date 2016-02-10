/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.execution.state;

import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Elias Ricken de Medeiros
 */
public class AbortingActivityWithBoundaryStateImpl extends EndingActivityWithBoundaryStateImpl {

    public AbortingActivityWithBoundaryStateImpl(final StateBehaviors stateBehaviors) {
        super(stateBehaviors);
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }

    @Override
    public int getId() {
        return 35;
    }

    @Override
    public String getName() {
        return "aborting activity with boundary";
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.ABORTING;
    }

    @Override
    public SStateCategory getBoundaryCategoryState() {
        return SStateCategory.ABORTING;
    }

}
