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
package org.bonitasoft.engine.events.model.builders;

import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;

/**
 * @author Baptiste Mesta
 * @author Christophe Havard
 */
public class SEventBuilderFactory {

    public SEventBuilder createNewInstance(final String type) {
        final SEvent event = new SEvent(type);
        return new SEventBuilder(event);
    }

    public SEventBuilder createInsertEvent(final String type) {
        final SEvent event = new SInsertEvent(type + SEvent.CREATED);
        return new SEventBuilder(event);
    }

    public SEventBuilder createDeleteEvent(final String type) {
        final SEvent event = new SDeleteEvent(type + SEvent.DELETED);
        return new SEventBuilder(event);
    }

    public SEventBuilder createUpdateEvent(final String type) {
        final SEvent event = new SUpdateEvent(type + SEvent.UPDATED);
        return new SEventBuilder(event);
    }

}
