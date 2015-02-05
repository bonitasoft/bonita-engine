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
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SACallActivityInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SACallActivityInstanceImpl extends SAActivityInstanceImpl implements SACallActivityInstance {

    private static final long serialVersionUID = -6431978996361790000L;

    public SACallActivityInstanceImpl() {
        super();
    }

    public SACallActivityInstanceImpl(final SCallActivityInstance activityInstance) {
        super(activityInstance);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.CALL_ACTIVITY;
    }

    @Override
    public String getDiscriminator() {
        return SACallActivityInstance.class.getName();
    }

    @Override
    public String getKind() {
        return "call";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SCallActivityInstance.class;
    }

}
