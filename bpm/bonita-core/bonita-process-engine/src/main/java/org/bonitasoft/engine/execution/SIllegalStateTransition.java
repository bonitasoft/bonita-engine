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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SIllegalStateTransition extends SActivityExecutionException {
    
    private static final long serialVersionUID = 6940283544247417112L;
    
    public SIllegalStateTransition(String message, Throwable cause) {
        super(message, cause);
    }

    public SIllegalStateTransition(String message) {
        super(message);
    }

    public SIllegalStateTransition(Throwable cause) {
        super(cause);
    }
    
}
