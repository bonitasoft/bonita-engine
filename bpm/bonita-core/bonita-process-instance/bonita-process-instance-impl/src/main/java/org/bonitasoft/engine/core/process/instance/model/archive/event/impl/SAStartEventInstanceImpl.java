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
import org.bonitasoft.engine.core.process.instance.model.archive.event.SAStartEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SStartEventInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SAStartEventInstanceImpl extends SACatchEventInstanceImpl implements SAStartEventInstance {

    private static final long serialVersionUID = 461897851088700885L;

    public SAStartEventInstanceImpl() {
        super();
    }

    public SAStartEventInstanceImpl(final SStartEventInstance startEventInstance) {
        super(startEventInstance);
    }

    @Override
    public String getDiscriminator() {
        return SAStartEventInstance.class.getName();
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.START_EVENT;
    }

    @Override
    public String getKind() {
        return "startEvent";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SStartEventInstance.class;
    }

}
