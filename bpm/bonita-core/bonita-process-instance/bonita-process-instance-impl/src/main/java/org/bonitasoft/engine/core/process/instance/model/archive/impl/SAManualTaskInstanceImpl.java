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
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SAManualTaskInstanceImpl extends SAHumanTaskInstanceImpl implements SAManualTaskInstance {

    private static final long serialVersionUID = -7214405802347593015L;

    public SAManualTaskInstanceImpl() {
        super();
    }

    public SAManualTaskInstanceImpl(final SManualTaskInstance sManualTaskInstance) {
        super(sManualTaskInstance);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.MANUAL_TASK;
    }

    @Override
    public String getDiscriminator() {
        return SAManualTaskInstanceImpl.class.getName();
    }

    @Override
    public String getKind() {
        return "manual";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SManualTaskInstance.class;
    }

}
