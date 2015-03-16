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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASubProcessActivityInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SASubProcessActivityInstanceImpl extends SAActivityInstanceImpl implements SASubProcessActivityInstance {

    private static final long serialVersionUID = -3846825095425055696L;

    private boolean triggeredByEvent;

    public SASubProcessActivityInstanceImpl() {
        super();
    }

    public SASubProcessActivityInstanceImpl(final SSubProcessActivityInstance activityInstance) {
        super(activityInstance);
        triggeredByEvent = activityInstance.isTriggeredByEvent();
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.SUB_PROCESS;
    }

    @Override
    public String getKind() {
        return "subProc";
    }

    @Override
    public String getDiscriminator() {
        return SASubProcessActivityInstance.class.getName();
    }

    @Override
    public boolean isTriggeredByEvent() {
        return triggeredByEvent;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SSubProcessActivityInstance.class;
    }

}
