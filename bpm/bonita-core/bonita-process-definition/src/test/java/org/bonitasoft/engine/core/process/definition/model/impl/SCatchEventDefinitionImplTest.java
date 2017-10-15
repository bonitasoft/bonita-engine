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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.event.impl.SCatchEventDefinitionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * 
 */
public class SCatchEventDefinitionImplTest {

    private SCatchEventDefinitionImpl catchEvent;

    @Before
    public void before() {
        catchEvent = new SCatchEventDefinitionImpl(9, "name") {

            private static final long serialVersionUID = 8249595229324418282L;

            @Override
            public SFlowNodeType getType() {
                return SFlowNodeType.AUTOMATIC_TASK;
            }
        };
    }

    @Test
    public void is_interrupting_if_interrupting_catch_event() {
        catchEvent.setInterrupting(true);

        assertTrue(catchEvent.isInterrupting());
    }

    @Test
    public void is_not_interrupting_if_non_interrupting_catch_event() {
        catchEvent.setInterrupting(false);

        assertFalse(catchEvent.isInterrupting());
    }

}
