/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SASubProcessActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SASubProcessActivityInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SASubProcessActivityInstanceBuilderImpl extends SAActivityInstanceBuilderImpl implements SASubProcessActivityInstanceBuilder {

    private SASubProcessActivityInstanceImpl entity;

    @Override
    public SASubProcessActivityInstanceBuilder createNewArchivedSubProcessActivityInstance(final SSubProcessActivityInstance subProcActInst) {
        entity = new SASubProcessActivityInstanceImpl(subProcActInst);
        return this;
    }

    @Override
    public SASubProcessActivityInstance done() {
        return entity;
    }

    @Override
    public String getTriggeredByEventKey() {
        return "triggeredByEvent";
    }

}
