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
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
public class SASendTaskInstanceImpl extends SAActivityInstanceImpl implements SASendTaskInstance {

    private static final long serialVersionUID = -3621349327932816690L;

    public SASendTaskInstanceImpl() {
        super();
    }

    public SASendTaskInstanceImpl(final SSendTaskInstance sSendTaskInstance) {
        super(sSendTaskInstance);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.SEND_TASK;
    }

    @Override
    public String getDiscriminator() {
        return SASendTaskInstanceImpl.class.getName();
    }

    @Override
    public String getKind() {
        return "send";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SSendTaskInstance.class;
    }

}
