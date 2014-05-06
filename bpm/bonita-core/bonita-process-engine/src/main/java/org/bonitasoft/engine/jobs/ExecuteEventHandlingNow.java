/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * 
 * Convenience command to execute the event handling job now. Internal use only.
 * 
 * 
 * @author Baptiste Mesta
 * 
 */
public class ExecuteEventHandlingNow extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandExecutionException {
        BPMEventHandlingJob bpmEventHandlingJob = new BPMEventHandlingJob();
        try {
            bpmEventHandlingJob.setAttributes(null);
            bpmEventHandlingJob.execute();
        } catch (SJobConfigurationException e) {
            throw new SCommandExecutionException("unable to execute event handling", e);
        } catch (SJobExecutionException e) {
            throw new SCommandExecutionException("unable to execute event handling", e);
        }
        return null;
    }

}
