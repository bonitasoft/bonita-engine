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
package org.bonitasoft.engine.core.process.instance.model.event.impl;

import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class SEventInstanceImpl extends SFlowNodeInstanceImpl implements SEventInstance {

    private static final long serialVersionUID = 2719962182085446506L;

    public SEventInstanceImpl() {
        super();
    }

    public SEventInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
    }

}
