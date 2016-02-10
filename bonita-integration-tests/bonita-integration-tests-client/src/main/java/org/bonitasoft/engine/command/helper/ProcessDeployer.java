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
package org.bonitasoft.engine.command.helper;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.command.helper.designer.SimpleProcessDesigner;
import org.bonitasoft.engine.exception.BonitaException;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:40
 */
public abstract class ProcessDeployer {

    ProcessDefinition processDefinition;

    public ProcessDefinition deploy(SimpleProcessDesigner design) throws BonitaException {
        return processDefinition = deploy(design.done());
    }

    public abstract ProcessDefinition deploy(DesignProcessDefinition design) throws BonitaException;

    public void clean() throws BonitaException {
        clean(processDefinition);
    }

    public abstract void clean(ProcessDefinition processDefinition) throws BonitaException;
}
