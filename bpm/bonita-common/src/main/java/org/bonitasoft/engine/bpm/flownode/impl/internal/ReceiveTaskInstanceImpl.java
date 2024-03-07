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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskInstance;

/**
 * @author Julien Molinaro
 */
public class ReceiveTaskInstanceImpl extends TaskInstanceImpl implements ReceiveTaskInstance {

    private static final long serialVersionUID = 4187673605124017954L;

    public ReceiveTaskInstanceImpl(final String name, final long flownodeDefinitionId) {
        super(name, flownodeDefinitionId);
    }

    @Override
    public FlowNodeType getType() {
        return FlowNodeType.RECEIVE_TASK;
    }

}
