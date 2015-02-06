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
package org.bonitasoft.engine.events.model.builders.impl;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.events.model.impl.SDeleteEventImpl;
import org.bonitasoft.engine.events.model.impl.SEventImpl;
import org.bonitasoft.engine.events.model.impl.SInsertEventImpl;
import org.bonitasoft.engine.events.model.impl.SUpdateEventImpl;

/**
 * @author Christophe Havard
 */
public class SEventBuilderFactoryImpl implements SEventBuilderFactory {

    @Override
    public SEventBuilder createNewInstance(final String type) {
        final SEvent event = new SEventImpl(type);
        return new SEventBuilderImpl(event);
    }

    @Override
    public SEventBuilder createInsertEvent(final String type) {
        final SEvent event = new SInsertEventImpl(type);
        return new SEventBuilderImpl(event);
    }

    @Override
    public SEventBuilder createDeleteEvent(final String type) {
        final SEvent event = new SDeleteEventImpl(type);
        return new SEventBuilderImpl(event);
    }

    @Override
    public SEventBuilder createUpdateEvent(final String type) {
        final SEvent event = new SUpdateEventImpl(type);
        return new SEventBuilderImpl(event);
    }

}
