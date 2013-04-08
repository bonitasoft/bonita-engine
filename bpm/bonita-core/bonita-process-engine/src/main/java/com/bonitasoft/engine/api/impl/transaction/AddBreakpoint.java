/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

import com.bonitasoft.engine.core.process.instance.api.BreakpointService;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;

/**
 * @author Baptiste Mesta
 */
public class AddBreakpoint implements TransactionContentWithResult<SBreakpoint> {

    private final BreakpointService breakpointService;

    private SBreakpoint breakpoint;

    public AddBreakpoint(final BreakpointService breakpointService, final BPMInstanceBuilders breakpointBuilder, final long definitionId,
            final long instanceId, final String elementName, final int idOfTheStateToInterrupt, final int idOfTheInterruptingState) {
        this.breakpointService = breakpointService;
        breakpoint = breakpointBuilder.getSBreakpointBuilder()
                .createNewInstance(definitionId, instanceId, elementName, idOfTheStateToInterrupt, idOfTheInterruptingState).done();
    }

    public AddBreakpoint(final BreakpointService breakpointService, final BPMInstanceBuilders breakpointBuilder, final long definitionId,
            final String elementName, final int idOfTheStateToInterrupt, final int idOfTheInterruptingState) {
        this.breakpointService = breakpointService;
        breakpoint = breakpointBuilder.getSBreakpointBuilder().createNewInstance(definitionId, elementName, idOfTheStateToInterrupt, idOfTheInterruptingState)
                .done();
    }

    @Override
    public void execute() throws SBonitaException {
        breakpoint = breakpointService.addBreakpoint(breakpoint);
    }

    @Override
    public SBreakpoint getResult() {
        return breakpoint;
    }

}
