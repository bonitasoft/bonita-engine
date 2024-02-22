/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.test.toolkit.exception;

import java.text.MessageFormat;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;

/**
 * @author Vincent Elcrin
 */
public class NextActivityIsNotAllowedStateException extends TestToolkitException {

    /**
     *
     */
    private static final long serialVersionUID = 8542205212242675994L;

    /**
     * Default Constructor.
     */
    public NextActivityIsNotAllowedStateException(final HumanTaskInstance humanTask) {
        super(MessageFormat.format("Activity with id {0} is the state {1} which is not allowed.", humanTask.getId(),
                humanTask.getState()));
    }

}
