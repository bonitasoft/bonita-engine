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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProcessInstanceUpdateBuilderImpl implements SProcessInstanceUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SProcessInstanceUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public SProcessInstanceUpdateBuilder updateLastUpdate(final long lastUpdate) {
        descriptor.addField(SProcessInstanceBuilderFactoryImpl.LAST_UPDATE_KEY, lastUpdate);
        return this;
    }

    @Override
    public SProcessInstanceUpdateBuilder updateInterruptingEventId(final long interruptingEventId) {
        descriptor.addField(SProcessInstanceBuilderFactoryImpl.INTERRUPTING_EVENT_ID_KEY, interruptingEventId);
        return this;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

}
