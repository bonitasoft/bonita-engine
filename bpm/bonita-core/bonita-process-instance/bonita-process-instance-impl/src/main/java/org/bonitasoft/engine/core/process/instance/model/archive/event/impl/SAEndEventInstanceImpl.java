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
import org.bonitasoft.engine.core.process.instance.model.archive.event.SAEndEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEndEventInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SAEndEventInstanceImpl extends SAThrowEventInstanceImpl implements SAEndEventInstance {

    private static final long serialVersionUID = -5873130765741009308L;

    public SAEndEventInstanceImpl() {
        super();
    }

    public SAEndEventInstanceImpl(final SEndEventInstance endEvent) {
        super(endEvent);
    }

    @Override
    public String getDiscriminator() {
        return SAEndEventInstance.class.getName();
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.END_EVENT;
    }

    @Override
    public String getKind() {
        return "endEvent";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SEndEventInstance.class;
    }

}
