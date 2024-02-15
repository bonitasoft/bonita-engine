/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.RuntimeCommand;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * @author Baptiste Mesta
 */
public class WaitServerCommand extends RuntimeCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final ServiceAccessor serviceAccessor)
            throws SCommandExecutionException {
        if (parameters.get("clear") != null) {
            serviceAccessor.getSynchroService().clearAllEvents();
            return null;
        }
        @SuppressWarnings("unchecked")
        final Map<String, Serializable> event = (Map<String, Serializable>) parameters.get("event");
        final int timeout = (Integer) parameters.get("timeout");
        try {
            return serviceAccessor.getSynchroService().waitForEvent(event, timeout);
        } catch (final Exception e) {
            throw new SCommandExecutionException(e);
        }
    }

}
