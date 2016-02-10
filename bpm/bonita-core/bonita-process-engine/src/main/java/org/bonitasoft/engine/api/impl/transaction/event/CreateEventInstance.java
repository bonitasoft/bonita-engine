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
package org.bonitasoft.engine.api.impl.transaction.event;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;

/**
 * @author Elias Ricken de Medeiros
 */
public class CreateEventInstance implements Callable<Void> {

    private final SEventInstance eventInstance;

    private final EventInstanceService eventInstanceService;

    public CreateEventInstance(final SEventInstance eventInstance, final EventInstanceService eventInstanceService) {
        this.eventInstanceService = eventInstanceService;
        this.eventInstance = eventInstance;
    }

    @Override
    public Void call() throws SBonitaException {
        this.eventInstanceService.createEventInstance(eventInstance);
        return null;
    }

}
