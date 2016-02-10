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
package org.bonitasoft.engine.core.process.instance.model.archive.event.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.archive.event.SAIntermediateThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateThrowEventInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Celine Souchet
 */
public class SAIntermediateThrowEventInstanceImpl extends SAThrowEventInstanceImpl implements SAIntermediateThrowEventInstance {

    private static final long serialVersionUID = -7858545825600523724L;

    public SAIntermediateThrowEventInstanceImpl() {
        super();
    }

    public SAIntermediateThrowEventInstanceImpl(final SIntermediateThrowEventInstance sIntermediateThrowEventInstance) {
        super(sIntermediateThrowEventInstance);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.INTERMEDIATE_THROW_EVENT;
    }

    @Override
    public String getKind() {
        return "intermediateThrowEvent";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SIntermediateThrowEventInstance.class;
    }

    @Override
    public String getDiscriminator() {
        return SAIntermediateThrowEventInstance.class.getName();
    }

}
