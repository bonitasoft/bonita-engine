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
package org.bonitasoft.engine.core.process.definition.exception;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProcessDefinitionNotFoundException extends SProcessDefinitionException {

    private static final long serialVersionUID = 1702422492322010491L;

    public SProcessDefinitionNotFoundException(final String message, final long id) {
        super(message);
        setProcessDefinitionIdOnContext(id);
    }

    public SProcessDefinitionNotFoundException(final Throwable cause, final long id) {
        super(cause);
        setProcessDefinitionIdOnContext(id);
    }

    public SProcessDefinitionNotFoundException(final String message, final Throwable e, final long id) {
        super(message, e);
        setProcessDefinitionIdOnContext(id);
    }

    public SProcessDefinitionNotFoundException(final Throwable cause, final SProcessDefinitionDeployInfo processDefinitionDeployInfo) {
        this(cause, processDefinitionDeployInfo.getId());
        setProcessDefinitionNameOnContext(processDefinitionDeployInfo.getName());
        setProcessDefinitionVersionOnContext(processDefinitionDeployInfo.getVersion());
    }

    public SProcessDefinitionNotFoundException(final String processName) {
        super("Can't find the process.");
        setProcessDefinitionNameOnContext(processName);
    }

}
