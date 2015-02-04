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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * A command is a class that is called from the API and executed on the server side.<br>
 * It is used to extend the engine behavior. See {@link org.bonitasoft.engine.api.CommandAPI} for explanations of how to deploy, undeploy and execute a command.
 * <br>
 * This class should not be directly subclassed by implementors: use {@link PlatformCommand} or {@link org.bonitasoft.engine.command.TenantCommand} instead.
 * 
 * @see org.bonitasoft.engine.api.CommandAPI
 * @see org.bonitasoft.engine.command.PlatformCommand
 * @see org.bonitasoft.engine.command.TenantCommand
 * @author Matthieu Chaffotte
 */
public interface Command<T extends ServiceAccessor> {

    /**
     * Method that is called by the engine on the server side when the client calls {@link CommandAPI#execute(String, Map)} with the name or id of this
     * command.
     * Implementors of commands must put here the code to be executed on the server side
     * 
     * @param parameters
     *        a map of parameters that can be used by the command and that is given by the client when executing the command
     * @param serviceAccessor
     *        the TenantServiceAccessor or PlatformServiceAccessor that provides access to the engine's server-side services
     * @return
     *         a result that will be returned to the client
     * @throws SCommandParameterizationException
     *         can be thrown if insufficient or wrong parameters are given by the client
     * @throws SCommandExecutionException
     *         can be thrown when something unexpected happens while executing the command
     */
    Serializable execute(Map<String, Serializable> parameters, T serviceAccessor) throws SCommandParameterizationException, SCommandExecutionException;

}
